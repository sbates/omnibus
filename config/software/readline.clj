(software "readline" :source "readline-5.2"
                     :steps [["./configure" "--prefix=/opt/opscode/embedded"]
                             ["make"]
                             ["sudo" "make" "install"]])
