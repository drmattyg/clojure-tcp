(load "tcp")
(def socket (com.savarese.rocksaw.net.RawSocket.))
(.open socket com.savarese.rocksaw.net.RawSocket/PF_INET (com.savarese.rocksaw.net.RawSocket/getProtocolByName "ip"))
(def source-ip (tcp/inet-to-long (java.net.Inet4Address/getLocalHost)))
(def dest-ip (tcp/get-ip-by-name "www.google.com"))


(def s (tcp/syn-packet-header 51976 80 800))
(def srcip (tcp/ip-to-long [127 0 0 1]))
(def destip (tcp/ip-to-long [10 0 1 1]))
(def s (tcp/set-checksum s srcip destip 0))
(.close socket)
(println "Done")
