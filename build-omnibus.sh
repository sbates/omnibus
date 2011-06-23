#!/bin/bash

export LEIN_ROOT=true
export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
cd /root/omnibus
git pull >/tmp/omnibus.out 2>&1 
lein run --project-name "$1" --bucket-name "$2" --s3-access-key "$3" --s3-secret-key "$4" >>/tmp/omnibus.out 2>&1 

