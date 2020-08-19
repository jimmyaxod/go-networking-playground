package main

import (
    "bufio"
    "fmt"
    "log"
    "os"
)

/*
 * Open a set of files and read them...
 *
 */
func main() {

    a := os.Args[1:]
    for i := 0; i < len(a); i++ {
        fmt.Printf("Arg %d %s\n", i, a[i])
	readfile(a[i])
	// We want to read it in...
    }

}

/*
 * Read file in and do something with the data...
 *
 */
func readfile(f string) {
    fmt.Printf("Reading %s\n", f)
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
}
