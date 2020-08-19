package main

import (
//    "bufio"
    "fmt"
    "time"
    "sync"
    "os"
)

/*
 * Open a set of files and read them...
 *
 */
func main() {
    var wg sync.WaitGroup

    starttime := time.Now().UnixNano()

    a := os.Args[1:]
    for i := 0; i < len(a); i++ {
        fmt.Printf("Arg %d %s\n", i, a[i])
	wg.Add(1)
	go readfile(&wg, a[i])
	// We want to read it in...
    }

    fmt.Println("Waiting for everything to finish")
    wg.Wait()

    endtime := time.Now().UnixNano()

    fmt.Fprintf(os.Stdout, "Took %dms\n", (endtime - starttime) / 1e6)

    os.Exit(0)
}

/*
 * Read file in and do something with the data...
 *
 */
func readfile(wg *sync.WaitGroup, f string) {
    defer wg.Done()

    fmt.Printf("Reading %s\n", f)

/*
    file, err := os.Open("/path/to/file.txt")
    if err != nil {
        log.Fatal(err)
    }
    defer file.Close()

    scanner := bufio.NewScanner(file)
    for scanner.Scan() {
        fmt.Println(scanner.Text())
    }

    if err := scanner.Err(); err != nil {
        log.Fatal(err)
    }
*/
}
