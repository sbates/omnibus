(software "chef" :source "chef"
                 :steps [[ "/opt/opscode/embedded/bin/gem" "install" "chef" "fog" "highline" "net-ssh-multi" "-n" "/opt/opscode/bin" "--no-rdoc" "--no-ri"]
                         [ "cp" "setup.rb" "/opt/opscode"]
                         [ "chmod" "755" "/opt/opscode/setup.rb"]
                         [ "rm" "-rf" "/opt/opscode/embedded/docs"]
                         [ "chown" "-R" (cond (is-os? "darwin") "root:wheel" true "root:root") "/opt/opscode"]])

