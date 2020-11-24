## What is this?

This is a simple chess "vision" game to help memorize chess
coordinates. 

When you click the correct square, a new coordinate is shown.

Try it out here: https://app.upgradingdave.com/chess-vision/

## Development

The following will build the code, watch for changes (and
automatically recompile) and start a server on port 8080. After
running this command, open a browser and go to http://localhost:8080

	npx shadow-cljs watch frontend
	
If you want a repl:

	npx shadow-cljs browser-repl
	
	
Release a new production version (advanced compile and then copy to
firebase project):

	./scripts/release.sh



