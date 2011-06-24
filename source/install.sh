#!/bin/bash

release_version="0.10.0-1"
use_shell=0

# Check whether a command exists - returns 0 if it does, 1 if it does not
exists() {
  if command -v $1 &>/dev/null
  then
    return 0
  else
    return 1 
  fi
}

# Set the filename for a deb, based on version and machine
deb_filename() {
  filetype="deb"
  if [ $machine = "x86_64" ]; 
  then
    filename="chef-full_${version}_amd64.deb"
  else
    filename="chef-full_${version}_${machine}.deb"
  fi
}

# Set the filename for an rpm, based on version and machine
rpm_filename() {
  filetype="rpm"
  filename="chef-full-${version}.${machine}.rpm"
}

# Set the filename for the sh archive
shell_filename() {
  filetype="sh"
  filename="chef-full-${version}-${platform}-${platform_version}-${machine}.sh"
}

# Get command line arguments
while getopts sv: opt
do
  case "$opt" in
    v)  version="$OPTARG";;
    s)  use_shell=1;;
    \?)		# unknown flag
        echo >&2 \
          "usage: $0 [-s] [-v version]"
          
        exit 1;;
  esac
done
shift `expr $OPTIND - 1`

machine=$(echo -e `uname -m`)

if [ -f "/etc/lsb-release" ];
then
  platform=$(cat /etc/lsb-release | grep DISTRIB_ID | cut -d "=" -f 2 | tr '[A-Z]' '[a-z]')
  platform_version=$(cat /etc/lsb-release | grep DISTRIB_RELEASE | cut -d "=" -f 2)
elif [ -f "/etc/debian_version" ];
then
  platform="debian"
  platform_version=$(echo -e `cat /etc/debian_version`)
elif [ -f "/etc/redhat-release" ];
then
  platform=$(echo -e `cat /etc/redhat-release` | perl -pi -e 's/^(.+) release.+/$1/' | tr '[A-Z]' '[a-z]')
  if [ "$platform" = "redhat enterprise linux server" ];
  then
    platform="el"
  elif [ "$platform" = "centos" ];
  then
    platform="el"
  elif [ "$platform" = "scientific" ];
  then
    platform="el"
  fi

  platform_version=$(echo -e `cat /etc/redhat-release` | perl -pi -e 's/^.+ release ([\d\.]+).*/$1/' | tr '[A-Z]' '[a-z]')

  if [ "$platform" = "el" ];
  then
    major_version=$(echo $platform_version | perl -pi -e 's/^(\d).+$/$1/g')
    case $platform in
      "5") platform_version="5.6" ;;
      "6") platform_version="6.0" ;;
    esac
  fi
elif [ -f "/etc/system-release" ];
then
  platform=$(echo -e `cat /etc/system-release` | perl -pi -e 's/^(.+) release.+/$1/' | tr '[A-Z]' '[a-z]')
  platform_version=$(echo -e `cat /etc/system-release` | perl -pi -e 's/^.+ release ([\d\.]+).*/$1/' | tr '[A-Z]' '[a-z]')
fi

if [ -z "$version" ];
then
  version=$release_version
fi

if [ $use_shell = 1 ];
then
  shell_filename
else
  case $platform in
    "ubuntu") deb_filename ;;
    "debian") deb_filename ;;
    "redhat") rpm_filename ;;
    "fedora") rpm_filename ;;
    "centos") rpm_filename ;;
  esac
fi

echo "Downloading Chef $version for $platform $platform_version ..."

if exists wget; 
then
  wget -O /tmp/$filename http://s3.amazonaws.com/opscode-full-stack/$platform-$platform_version-$machine/$filename
else
  if exists curl;
  then
    curl http://s3.amazonaws.com/opscode-full-stack/$platform-$platform_version-$machine/$filename > /tmp/$filename
  else
    echo "Cannot find wget or curl - cannot install Chef!"
    exit 5
  fi
fi

echo "Installing Chef $version"
case "$filetype" in
  "rpm") rpm -Uvh /tmp/$filename ;;
  "deb") dpkg -i /tmp/$filename ;;
  "sh" ) bash /tmp/$filename ;;
esac
