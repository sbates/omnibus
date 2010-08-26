(software "libxml2" :source "libxml2-2.7.7"
                    :steps [["./configure" "--prefix=/opt/opscode/embedded" "--with-zlib=/opt/opscode/embedded" "--with-readline=/opt/opscode/embedded" "--with-iconv=/opt/opscode/embedded"]
                            ["make"]
                            ["sudo" "make" "install"]])
