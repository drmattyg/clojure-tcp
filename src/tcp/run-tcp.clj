(load "tcp")
(def socket (com.savarese.rocksaw.net.RawSocket.))
(.open socket com.savarese.rocksaw.net.RawSocket/PF_INET (com.savarese.rocksaw.net.RawSocket/getProtocolByName "ip"))
(.setIPHeaderInclude socket true)
(println socket)
(.close socket)
(tcp/dump-packet 10 20 4 0)
(println "Done")
