CC=arm-apple-darwin-gcc
LD=$(CC)
CFLAGS=-std=gnu99 -Isrc/numbers_big -DBUILTIN_FONTS -wall
LDFLAGS=-lobjc -framework CoreFoundation -framework Foundation -framework UIKit -framework  LayerKit -framework CoreSurface -framework GraphicsServices

all:	DaliClock package

DaliClock:	src/main.o src/DaliClockApplication.o src/digital.o src/DaliClockView.o src/hsv.o src/logdebug.o
	$(LD) $(LDFLAGS) -o $@ $^

%.o:	%.m
	$(CC) -c $(CFLAGS) $(CPPFLAGS) $< -o $@

clean:
	rm -f src/*.o DaliClock
	rm -rf build
	
package:
	rm -rf build
	mkdir build
	cp -r src/DaliClock.app ./build
	mv DaliClock ./build/DaliClock.app