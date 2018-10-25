These dictionaries are in strict TSV format (same number of columns on
every line), so they can be viewed easily in GitHub.

Make sure your editor doesn't truncate trailing tabs!  The
`.editorconfig` file at the root of the repository will configure many
editors correctly.

If editing, make sure you add tabs, and not spaces.

You could check your results with something like
```
for i in *.tsv; do
	echo $i
	tr -dc '\t\n' < $i | tr '\t' _ | uniq
done
```
and see that only one line is output from each file.
