default:
	cat Makefile

oauth:
	sudo hostess add sportball.test 127.0.0.1
	echo "rdr pass inet proto tcp from any to any port 80 -> 127.0.0.1 port 3000" | sudo pfctl -ef -

deoauth:
	sudo hostess add sportball.test 127.0.0.1
	#sudo pfctl -f

start:
	lein run -- --port=3000 &
	lein figwheel

deploy:
	lein uberjar
	git push heroku
