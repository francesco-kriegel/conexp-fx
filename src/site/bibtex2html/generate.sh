#!/bin/bash

source ~/.bash_profile

mkdir -p target/bibtex2html
bibtex2html -nodoc -nobibsource -o target/bibtex2html/references src/site/bibtex2html/references.bib
perl -0777 -i'' -pe '1 while s/\&lt\;b\&gt\;/<b>/g' target/bibtex2html/references.html
perl -0777 -i'' -pe '1 while s/\&lt\;\/b\&gt\;/<\/b>/g' target/bibtex2html/references.html
echo "<table>$(perl -MHTML::TreeBuilder -le '
  $html = HTML::TreeBuilder->new_from_file($ARGV[0]) or die $!;
  foreach ($html->look_down(_tag => "table")) {
    print map {$_->as_HTML()} $_->content_list();
  }' target/bibtex2html/references.html)</table>" > target/bibtex2html/references.html
cat src/site/markdown/mathematical-background.md.tmp target/bibtex2html/references.html > src/site/markdown/mathematical-background.md
