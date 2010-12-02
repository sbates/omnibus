(software "curl" :source "curl-7.21.2"
          :steps [
                  ["./configure" "--prefix=/opt/opscode/embedded" "--disable-debug" "--enable-optimize" "--disable-ldap" "--disable-ldaps" "--disable-rtsp" "--enable-proxy" "--disable-dependency-tracking" "--enable-ipv6" "--without-libidn" "--with-ssl=/opt/opscode/embedded/lib" "--with-zlib=/opt/opscode/embedded/lib" ]
                  ["make"]
                  ["make" "install"]
                  ])


