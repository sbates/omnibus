

(software "gdbm" :source "gdbm-1.8.3"
                 :steps [["env" "LDFLAGS=-R/opt/opscode/embedded/lib -L/opt/opscode/embedded/lib -I/opt/opscode/embedded/include" "CFLAGS=-L/opt/opscode/embedded/lib -I/opt/opscode/embedded/include" "./configure" "--prefix=/opt/opscode/embedded"]
                         (if (is-os? "darwin") ["perl" "-pi" "-e" "s/BINOWN = bin/BINOWN = root/g" "Makefile"])
                         (if (is-os? "darwin") ["perl" "-pi" "-e" "s/BINGRP = bin/BINGRP = wheel/g" "Makefile"])
                         ["make"]
                         ["make" "install"]])
