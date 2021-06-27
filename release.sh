FILENAME='alpha6.keystore'
lein prod-build &&
printf '1j2k3lj35k2l\n1j2k3lj35k2l\nDaniel Heiniger\nRedeemer Norwalk\nRedeemer\nNorwalk\nIowa\nUS\nyes' | keytool -genkey -v -keystore $FILENAME -alias alpha -keyalg RSA -keysize 2048 -validity 10000 &&
mv $FILENAME ./android/app &&
cd android &&
./gradlew assembleRelease
