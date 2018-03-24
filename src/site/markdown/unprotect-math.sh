perl -0777 -i'' -pe '1 while s/\s*<div>\s*<pre>\s*inlinemath:/ \$/s' target/site/mathematical-background.html
perl -0777 -i'' -pe '1 while s/\s*<div>\s*<pre>\s*displaymath:/ \$\$/s' target/site/mathematical-background.html
perl -0777 -i'' -pe '1 while s/:inlinemath\s*<\/pre>\s*<\/div>/\$/s' target/site/mathematical-background.html
perl -0777 -i'' -pe '1 while s/:displaymath\s*<\/pre>\s*<\/div>/\$\$/s' target/site/mathematical-background.html
