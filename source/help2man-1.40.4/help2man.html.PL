#!/usr/bin/perl

#
# Combine generated html page with GNU boilerplate.
#

use strict;
use warnings;
use File::Temp;
use Getopt::Long;

my %opts;
die "Usage: $0 [--quiet] [--stdout]\n"
    unless GetOptions \%opts, qw(quiet stdout) and !@ARGV;

undef $/;

# Fetch GNU boilerplate
my $boilerplate;
my $url = 'http://www.gnu.org/server/standards/boilerplate-source.html';
do {
    open my $b, '-|', 'curl', '-sL', $url or die "curl: $!";
    $boilerplate = <$b>;
    ($url) = $boilerplate =~ /<meta\s+http-equiv=["']?refresh["']?\s+
	                      content=["']\d+;\s+url=["']?(http[^"'>]*)/xi;
} while $url;

for ($boilerplate)
{
    s#\$Revision:\s+(\S+)\s+\$#$1#;
    s#<!-- This is the template document.*?-->\s+##s;
    s#<!-- Instructions for adapting.*?-->\s*(<!-- \d+\. .*?-->\s*)*##s;
    s#<title>Baz\s+(- GNU Project)#<title>help2man $1#s;
    s#<h2>GNU\sBaz</h2>.*(</div><!--\s+for\s+id="content")#%body%$1#s;
}

my ($header, $footer) = split /%body%/, $boilerplate;
die "can't parse boilerplate" unless $footer;

# Generate manual from texinfo
my $texi_tmp = File::Temp->new();
system 'makeinfo', '--html', '--no-number-sections', '--no-headers',
    '--no-split', '--output=' . $texi_tmp->filename, 'help2man.texi';

my $gnu_standards = "http://www.gnu.org/prep/standards/standards.html";
my $body = <$texi_tmp>;
for ($body)
{
    s#^.*<body>##s;
    s#</body>.*##s;

    # Fixup references
    s#<a\s+href="standards\.html#<a href="$gnu_standards#g;
    s#<a\s+href="\*manpages\*\.html\#perlre"
        #<a href="http://perldoc.perl.org/perlre.html"#xg;

    # Drop heading sizes by one, as h1 is quite loud.
    s#<(/?)h(\d)\b#"<${1}h" . ($2 + 1)#ge;
}

# Write output
my $target = $0;
my $out;
if ($opts{stdout})
{
    $out = \*STDOUT;
    $opts{quiet} = 1;
}
else
{
    $target =~ s!.*/!!;
    $target =~ s/\.PL$//  or die "$0: can't determine target name\n";
    unlink $target        or die "$0: can't unlink $target ($!)\n"
	if -e $target;
    open $out, ">$target" or die "$0: can't create $target ($!)\n";
}

print "Extracting $target (with GNU boilerplate)\n"
    unless $opts{quiet};

print $out $header, $body, $footer;

# Fix output file permissions
unless ($opts{stdout})
{
    close $out;
    chmod 0444, $target or warn "$0: can't change mode of $target ($!)\n";
}
