#!/bin/bash

cd ./chef-repo

mkdir -p build-output

echo "Centos 5.5 x86_64"
knife rackspace server create -r 'role[fatty]' -I 51 -f 4 -d centos5-gems > ./build-output/centos-5.5-x86_64.txt &
echo "Centos 5.4 x86_64"
knife rackspace server create -r 'role[fatty]' -I 187811 -f 4 -d centos5-gems > ./build-output/centos-5.4-x86_64.txt & 
echo "Fedora 13 x86_64"
knife rackspace server create -r 'role[fatty]' -I 53 -f 4 -d fedora13-gems > ./build-output/fedora-13-x86_64.txt &
echo "Fedora 14 x86_64"
knife rackspace server create -r 'role[fatty]' -I 71 -f 4 -d fedora13-gems > ./build-output/fedora-14-x86_64.txt &
echo "Ubuntu 10.04 LTS x86_64"
knife rackspace server create -r 'role[fatty]' -I 49 -f 4 -d ubuntu10.04-gems > ./build-output/ubuntu-10.04-x86_64.txt &
echo "Ubuntu 10.10 x86_64"
knife rackspace server create -r 'role[fatty]' -I 69 -f 4 -d ubuntu10.04-gems > ./build-output/ubuntu-10.10-x86_64.txt &
echo "Debian 5.0 (lenny) x86_64"
knife rackspace server create -r 'role[fatty]' -I 4 -f 4 -d ubuntu10.04-gems > ./build-output/debian-5.0-x86_64.txt &

cd ..
