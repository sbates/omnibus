(software "zlib"  :source "zlib-1.2.5" :steps [["./configure" "--prefix=/opt/opscode/embedded"]
                                               ["make"]
                                               ["sudo" "make" "install"]])
