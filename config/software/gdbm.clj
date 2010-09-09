

(software "gdbm" :source "gdbm-1.8.3"
                 :steps [["./configure" "--prefix=/opt/opscode/embedded"]
                         (if (is-os? "darwin") ["perl" "-pi" "-e" "s/BINOWN = bin/BINOWN = root/g" "Makefile"])
                         (if (is-os? "darwin") ["perl" "-pi" "-e" "s/BINGRP = bin/BINGRP = wheel/g" "Makefile"])
                         ["make"]
                         [ "make" "install"]])
