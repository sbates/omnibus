(software "libiconv" :source "libiconv-1.13.1"
                     :steps [["./configure" "--prefix=/opt/opscode/embedded"]
                             ["make"]
                             ["sudo" "make" "install"]])
