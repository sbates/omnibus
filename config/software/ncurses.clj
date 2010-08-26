(software "ncurses" :source "ncurses-5.7"
                    :steps [["./configure" "--prefix=/opt/opscode/embedded" "--with-shared" "--with-normal" "--without-debug"]
                            ["make"]
                            ["sudo" "make" "install"]])
