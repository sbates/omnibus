(software "openssl" :source "openssl-0.9.8o"
                    :steps [
                            (cond
                              (and (is-os? "darwin") (is-machine? "x86_64")) ["./Configure" "darwin64-x86_64-cc" "--prefix=/opt/opscode/embedded" "--with-zlib-lib=/opt/opscode/embedded/lib" "--with-zlib-include=/opt/opscode/embedded/include" "zlib" "shared"]
                              true ["./config" "--prefix=/opt/opscode/embedded" "--with-zlib-lib=/opt/opscode/embedded/lib" "--with-zlib-include=/opt/opscode/embedded/include" "zlib" "shared"])
                            ["bash" "-c" "make"]
                            ["sudo" "make" "install"]])
