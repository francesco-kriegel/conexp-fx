perl -0777 -i'' -pe '1 while s/(^[^\$]*)?\$\$([^\$]+)\$\$/$1<pre>displaymath:$2:displaymath<\/pre>/s' src/site/markdown/mathematical-background.md
perl -0777 -i'' -pe '1 while s/(^[^\$]*)?\$([^\$]+)\$/$1<pre>inlinemath:$2:inlinemath<\/pre>/s' src/site/markdown/mathematical-background.md
