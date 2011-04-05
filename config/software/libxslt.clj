(software "libxslt" :source "libxslt-1.1.26"
                    :steps [["env" "LDFLAGS=-R/opt/opscode/embedded/lib -L/opt/opscode/embedded/lib -I/opt/opscode/embedded/include" "CFLAGS=-L/opt/opscode/embedded/lib -I/opt/opscode/embedded/include" "./configure" "--prefix=/opt/opscode/embedded" "--with-libxml-prefix=/opt/opscode/embedded" "--with-libxml-include-prefix=/opt/opscode/embedded/include" "--with-libxml-libs-prefix=/opt/opscode/embedded/lib"]
                            ["./buildit.sh"]
                            [ "make" "install"]])
