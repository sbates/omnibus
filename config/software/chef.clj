(software "chef" :source "chef"
                 :steps [["sudo" "/opt/opscode/embedded/bin/gem" "install" "chef" "fog" "highline" "net-ssh-multi" "-n" "/opt/opscode/bin" "--no-rdoc" "--no-ri"]
                         ["sudo" "cp" "setup.rb" "/opt/opscode"]
                         ["sudo" "chmod" "755" "/opt/opscode/setup.rb"]
                         ["sudo" "rm" "-rf" "/opt/opscode/embedded/docs"]
                         ["sudo" "chown" "-R" (cond (is-os? "darwin") "root:wheel" true "root:root") "/opt/opscode"]])

