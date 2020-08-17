/* Simple start to go programming
 *
 */
package main

import (
    "net"
    "os"
    "fmt"
    "time"
    "strconv"
)

/*
 * Simple port scanner in go. One of my first go programs!
 *
 * Usage is <host>
 *
 * eg portscan.go google.com
 */
func main() {
    target := os.Args[1]

    starttime := time.Now().UnixNano()

    for port := 1; port < 65536; port++ {
        fmt.Fprintf(os.Stdout, "Portscanner %s %d\n", target, port)

        tcpAddr, err := net.ResolveTCPAddr("tcp4", target + ":" + strconv.Itoa(port))
        checkError(err)

	go func(port int) {
        	conn, err := net.DialTCP("tcp", nil, tcpAddr)
		if err == nil {
	        	fmt.Fprintf(os.Stdout, "OPEN %s %d\n", target, port)

			conn.Close();
		} else {
	        	fmt.Fprintf(os.Stdout, "ERROR %s\n", err.Error())			
		}
	}(port)
    }

    endtime := time.Now().UnixNano()

    fmt.Fprintf(os.Stdout, "Took %dms\n", (endtime - starttime) / 1e6)

    os.Exit(0)
}

func checkError(err error) {
    if err != nil {
        fmt.Fprintf(os.Stderr, "Fatal error: %s", err.Error())
        os.Exit(1)
    }
}