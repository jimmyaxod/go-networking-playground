/* Simple start to go programming
 *
 */
package main

import (
    "net"
    "os"
    "fmt"
    "io/ioutil"
)

/*
 * Usage is <host>
 * eg first.go google.com
 */
func main() {
    sendData := []byte("HEAD / HTTP/1.0\r\n\r\n")

    target := os.Args[1]

    tcpAddr, err := net.ResolveTCPAddr("tcp4", target + ":80")
    checkError(err)

    conn, err := net.DialTCP("tcp", nil, tcpAddr)
    checkError(err)

    _, err = conn.Write(sendData)
    checkError(err)

    result, err := ioutil.ReadAll(conn)
    checkError(err)

    fmt.Println(string(result))

    os.Exit(0)
}

func checkError(err error) {
    if err != nil {
        fmt.Fprintf(os.Stderr, "Fatal error: %s", err.Error())
        os.Exit(1)
    }
}
