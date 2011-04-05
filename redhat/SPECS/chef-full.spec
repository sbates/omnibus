#
# RPM Spec for Chef-full

Summary: Chef is a Systems integration framework written in Ruby
Name: chef-full
Version: 0.9.12 
Release: 1
License: Apache 2.0
Group: Applications/System
Source: http://www.opscode.com 
URL: http://www.opscode.com
Vendor: Opscode, Inc.
Packager: Adam Jacob <adam@opscode.com>

%description
Chef is a systems integration framework and configuration management library
written in Ruby. Chef provides a Ruby library and API that can be used to
bring the benefits of configuration management to an entire infrastructure.
Chef can be run as a client (chef-client) to a server, or run as a standalone
tool (chef-solo). Configuration recipes are written in a pure Ruby DSL.  This
package contains a fully working stack, including Ruby, installed in
/opt/opscode.

%prep
rm -rf ${RPM_BUILD_ROOT}/opt
mkdir -p ${RPM_BUILD_ROOT}/opt
cp -r /opt/opscode ${RPM_BUILD_ROOT}/opt

%files
/opt/opscode
