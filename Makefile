CLASSPATH=../../..
SOURCPATH=../../..
JFLAGS=-classpath $(CLASSPATH)
JDFLAGS=-classpath $(CLASSPATH) -sourcepath $(SOURCPATH)
JAVAC=javac $(JFLAGS)
JAVA=java $(JFLAGS)
JAVADOC=javadoc $(JDFLAGS)
JLEX=$(JAVA) $(JFLAGS) JFlex.Main
CVS=cvs

all: compile build javadoc htmlsource

compile: buildclean CSVLexer.java \
	BrowserCommandLexer.java \
	CGILexer.java \
	ExcelCSVLexer.java
	$(JAVAC) *.java

CSVLexer.java: CSVLexer.lex
	$(JLEX) CSVLexer.lex

CGILexer.java: CGILexer.lex
	$(JLEX) CGILexer.lex

BrowserCommandLexer.java: BrowserCommandLexer.lex
	$(JLEX) BrowserCommandLexer.lex
		
ExcelCSVLexer.java: ExcelCSVLexer.lex
	$(JLEX) ExcelCSVLexer.lex

junkclean:
	rm -f *~ ~* utils_*.jar out.txt *.bak CSVTest.txt CircularBufferTestResults.txt com/ gnu/ src/

buildclean: junkclean
	rm -f utils.jar
        
javadocclean: junkclean
	rm -rf doc/

htmlsourceclean: junkclean
	rm -rf *.java.html syntax.css

clean: buildclean javadocclean htmlsourceclean
	rm -f *.class
        
allclean: clean
	rm -f CSVLexer.java BrowserCommandLexer.java CGILexer.java ExcelCSVLexer.java

javadoc: javadocclean
	mv -f package.html temp
	mkdir doc
	$(JAVADOC) -link http://java.sun.com/j2se/1.3/docs/api/ -d doc/ com.Ostermiller.util
	mv -f temp package.html

build: buildclean compile
	mkdir -p com/Ostermiller/util
	cp *.* Makefile com/Ostermiller/util/
	mkdir -p gnu/getopt		
	cp ../../../gnu/getopt/*.* gnu/getopt
	jar cfv utils.jar com/ gnu/
	rm -rf com/ gnu/

test: compile
	$(JAVA) com.Ostermiller.util.TokenizerTests > out.txt
	diff out.txt TokenizerTestResults.txt
	$(JAVA) com.Ostermiller.util.CSVLexer CSVRegressionTest.csv > out.txt
	diff out.txt CSVRegressionTestResults.txt
	$(JAVA) com.Ostermiller.util.ExcelCSVLexer ExcelCSVRegressionTest.csv > out.txt
	diff out.txt ExcelCSVRegressionTestResults.txt
	$(JAVA) com.Ostermiller.util.CSVTest > out.txt
	diff out.txt CSVTestResults.txt
	$(JAVA) com.Ostermiller.util.CircularBufferTests > out.txt
	diff out.txt CircularBufferTestResults.txt
	rm out.txt CSVTest.txt CircularBufferTestResults.txt
        
update: clean
	$(CVS) update
        
commit: clean
	$(CVS) commit

release: update commit all
	./release.sh

install:
	./install.sh

htmlsource:
	mkdir src
	cp *.java src
	$(JAVA) com.Ostermiller.util.Tabs -s 4 src/*.java
	$(JAVA) com.Ostermiller.Syntax.ToHTML -t src.bte -i whitespace src/*.java
	mv src/*.java.html src/*.css .
	rm -rf src
	
