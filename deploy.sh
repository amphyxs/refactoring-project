ssh -p 2222 s367527@helios.cs.ifmo.ru 'cd ~/is/is-cw/stage-3 && git pull && mvn clean install && nohup java -XX:MaxHeapSize=1G -XX:MaxMetaspaceSize=128m -jar target/parapp-0.0.1-SNAPSHOT.jar > app.log 2>&1 &'
ssh -L 18125:localhost:18124 -p 2222 s367527@helios.cs.ifmo.ru
