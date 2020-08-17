/* Simple start to go programming
 *
 */
package main

import (
    "net"
    "os"
    "sync"
    "fmt"
    "time"
)

/*
 * Simple port scanner in go. One of my first go programs!
 *
 * Usage is <host>
 *
 * eg portscan.go google.com
 */
func main() {
    var wg sync.WaitGroup

    timeout := 500 * time.Millisecond

    target := os.Args[1]

    starttime := time.Now().UnixNano()

    for port := 1; port < 65536; port++ {
	where := fmt.Sprintf("%s:%d", target, port)

	wg.Add(1)
	go func(ww string) {
                defer wg.Done()
		conn, err := net.DialTimeout("tcp", ww, timeout)
		if err == nil {
	        	fmt.Fprintf(os.Stdout, "OPEN %s\n", where)

			conn.Close();
		} else {
//	        	fmt.Fprintf(os.Stdout, "ERROR %s\n", err.Error())			
		}
	}(where)
    }

    fmt.Println("Waiting for everything to finish...")
    wg.Wait()

    endtime := time.Now().UnixNano()

    fmt.Fprintf(os.Stdout, "Took %dms\n", (endtime - starttime) / 1e6)

    os.Exit(0)
}

