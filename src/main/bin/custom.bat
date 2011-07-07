rem
rem inq.bat calls this file and places %INQCUSTOM% on the vm arguments
rem Put whatever you like in here accordingly, for example:
rem set INQCUSTOM=-Djndi.server=localhost -Djndi.port=1199 -Djava.naming.factory.initial=org.jnp.interfaces.NamingContextFactory -Djava.naming.provider.url=jnp://localhost:1199 -Djava.naming.factory.url.pkgs=org.something.here.naming:org.jnp.interfaces
rem

set INQCUSTOM=-Dfile.encoding=UTF-8
