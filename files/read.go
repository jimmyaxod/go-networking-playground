package main

import (
    "bufio"
    "fmt"
    "time"
    "sync"
    "os"
    "log"
)

type Counter struct {
	mu	sync.Mutex
	x	int64
}

func (c *Counter) Add(x int64) {
	c.mu.Lock()
	c.x += x                                       
	c.mu.Unlock()
}

func (c *Counter) Value() (x int64) {
	c.mu.Lock()
	x = c.x
	c.mu.Unlock()
	return
}
	

/*
 * Open a set of files and read them...
 *
 */
func main() {
    var wg sync.WaitGroup
    var totallines Counter

    starttime := time.Now().UnixNano()

    a := os.Args[1:]
    for i := 0; i < len(a); i++ {
        fmt.Printf("Arg %d %s\n", i, a[i])
        wg.Add(1)
        go readfile(&wg, &totallines, a[i])
        // We want to read it in...
    }

    fmt.Println("Waiting for everything to finish")
    wg.Wait()

    endtime := time.Now().UnixNano()

    fmt.Fprintf(os.Stdout, "Total lines read: %d\n", totallines.Value())
    fmt.Fprintf(os.Stdout, "Took %dms\n", (endtime - starttime) / 1e6)

    os.Exit(0)
}

/*
 * Read file in and do something with the data...
 *
 */
func readfile(wg *sync.WaitGroup, totallines *Counter, f string) {
    defer wg.Done()

    fmt.Printf("Reading %s\n", f)


    file, err := os.Open(f)
    if err != nil {
        log.Fatal(err)
    }
    defer file.Close()

    count := int64(0)
    
    scanner := bufio.NewScanner(file)
    for scanner.Scan() {
        fmt.Println(scanner.Text())
        count++
    }

    scanerr := scanner.Err()
    if scanerr != nil {
        log.Fatal(scanerr)
    }

    totallines.Add(count)
}
