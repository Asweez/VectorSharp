# SEE https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javapackager.html

JAVA_HOME=`/usr/libexec/java_home -v 1.8`
APP_DIR_NAME=Vector#
icon=package/windows/Vector#.ico
appVersion="1.0.0"

#-deploy -Bruntime=/Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home \
javapackager \
  -deploy -Bruntime=${JAVA_HOME} \
  -native exe \
  -srcdir . \
  -srcfiles Vector#.jar \
  -outdir release_windows \
  -outfile ${APP_DIR_NAME} \
  -appclass vectorsharp.VectorSharp \
  -name "Vector#" \
  -title "Vector#" \
  -nosign \
  -v

echo ""
echo "If that succeeded, it created \"release/bundles/${APP_DIR_NAME}\""