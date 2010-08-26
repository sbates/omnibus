(software "rsync" :source "rsync-3.0.7"
                 :steps [["./configure" "--prefix=/opt/opscode/embedded"]
                         ["make"]
                         ["sudo" "make" "install"]])

