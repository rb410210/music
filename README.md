# Music Manager

mvn clean package
sudo mkdir /usr/local/MusicManager
sudo cp ./target/music*.jar /usr/local/MusicManager/MusicManager.jar
sudo cp ./mp3download /usr/sbin/
sudo chmod 755 /usr/sbin/mp3download