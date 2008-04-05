/* xdaliclock - a melting digital clock
 * Copyright (c) 1991-2006 Jamie Zawinski <jwz@jwz.org>
 *
 * Permission to use, copy, modify, distribute, and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation.  No representations are made about the suitability of this
 * software for any purpose.  It is provided "as is" without express or
 * implied warranty.
 */

#import <CoreSurface/CoreSurface.h>
#import <GraphicsServices/GraphicsServices.h>
#import "DaliClockView.h"
#import "xdaliclock.h"
#import "hsv.h"
#import <sys/time.h>

static DaliClockView *sharedInstance = nil;
void updateScreen() {
	[sharedInstance performSelectorOnMainThread:@selector(updateScreen) withObject:nil waitUntilDone: NO];
}

int __screenOrientation;

@implementation DaliClockView 
- (id)initWithFrame:(struct CGRect)frame {
	NSLog(@"fsw: %f, fsh: %f", frame.size.width, frame.size.height);
	
	if ((self == [super initWithFrame:frame])!=nil) {
            sharedInstance = self;
            initGraphics = 0;
	}
	
	self = [super initWithFrame:frame];
  
	memset (&config, 0, sizeof(config));

	config.max_fps = 12;
	config.max_cps = 12;
	config.twelve_hour_p = 1;
	
	fgh = 1.0 * (240.0/360.0);
	bgh = 0.5;
	fgs = bgs = 1.0;
	fgv = bgv = 0.7;
	
	fgcolor = 0xffff;
	bgcolor = 0x0000;

	// initialize the fonts and bitmaps
	[self intializeDigits:frame];

	[self clockTick];
	[self colorTick];

	return self;
}

- (void)updateScreen {
	[sharedInstance setNeedsDisplay];
}

- (void)dealloc {
        [ screenLayer release ];
        pthread_mutex_destroy(&screenUpdateMutex);
        pthread_cond_destroy(&screenUpdateLock);
	[super dealloc];
}

- (void)drawRect:(CGRect)rect{
    if (initGraphics == 0) {
        int i;
        CFMutableDictionaryRef dict;
        int w = (int) rect.size.width;
        int h = (int) rect.size.height;

        int pitch = w * 2, allocSize = 2 * w * h;
        char *pixelFormat = "565L";

        initGraphics = 1;
        pthread_cond_init(&screenUpdateLock, NULL);
        pthread_mutex_init(&screenUpdateMutex, NULL);

        dict = CFDictionaryCreateMutable(kCFAllocatorDefault, 0,
            &kCFTypeDictionaryKeyCallBacks, &kCFTypeDictionaryValueCallBacks);

        CFDictionarySetValue(dict, kCoreSurfaceBufferGlobal, kCFBooleanTrue);
        CFDictionarySetValue(dict, kCoreSurfaceBufferMemoryRegion, CFSTR("PurpleGFXMem"));
        CFDictionarySetValue(dict, kCoreSurfaceBufferPitch, CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, &pitch));
        CFDictionarySetValue(dict, kCoreSurfaceBufferWidth, CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, &w));
        CFDictionarySetValue(dict, kCoreSurfaceBufferHeight, CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, &h));
        CFDictionarySetValue(dict, kCoreSurfaceBufferPixelFormat, CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, pixelFormat));
        CFDictionarySetValue(dict, kCoreSurfaceBufferAllocSize, CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, &allocSize));
		
        screenSurface = CoreSurfaceBufferCreate(dict);
        CoreSurfaceBufferLock(screenSurface, 3);

        screenLayer = [[LKLayer layer] retain];
        [screenLayer setFrame: CGRectMake(0.0f, 0.0f, w, h)];

        [screenLayer setContents: screenSurface];
        [screenLayer setOpaque: YES];
        [[self _layer] addSublayer: screenLayer];

        CoreSurfaceBufferUnlock(screenSurface);
    }

	unsigned short *c;
	c = CoreSurfaceBufferGetBaseAddress(screenSurface);
	
	/* Vertically center the clock */
	
	for (int y = 0; y < (((int) rect.size.height / 2) - (config.height / 2)); y++) {
		for (int x = 0; x < rect.size.width; x++) {
			*c++ = bgcolor;
		}
	}
	
	/* Horizontally center the clock */
	for (int x = 0;  x < (((int) rect.size.width) - config.width) / 2; x++) {
		*c++ = bgcolor;
	}
	
	unsigned char *scanin  = config.bitmap;

	for (int y = 0; y < config.height; y++)
  	{
    	for (int x = 0; x < config.width; x++)
    	{
      		unsigned char bit = scanin[x>>3] & (1 << (7 - (x & 7)));
      		
      		if (bit)
			{
				*c++ = fgcolor;
			}
			else
			{
				*c++ = bgcolor;
			}
  		}
		for (int x = 0; x < ((int) rect.size.width) - config.width; x++) {
			*c++ = bgcolor;
		}
		scanin += (config.width + 7) >> 3;
	}
	
	for (int y = 0; y < (((int) rect.size.height / 2) - (config.height / 2)) - 1; y++) {
		for (int x = 0; x < rect.size.width; x++) {
			*c++ = bgcolor;
		}
	}
}

/* When this timer goes off, we re-generate the bitmap/pixmap,
   and mark the display as invalid.
*/
- (void)clockTick
{
  if (clockTimer && [clockTimer isValid]) {
    [clockTimer invalidate];
    clockTimer = 0;
  }

  if (config.max_fps <= 0) abort();

    struct timeval now;
    struct timezone tzp;
    gettimeofday (&now, &tzp);
    render_once (&config, now.tv_sec, now.tv_usec);

    [self updateScreen];
 

  // re-schedule the timer according to current fps.
  //
  float delay = 0.9 / config.max_fps;
  clockTimer = [NSTimer scheduledTimerWithTimeInterval:delay
                                                target:self
                                              selector:@selector(clockTick)
                                              userInfo:nil
                                               repeats:NO];
}

- (void) intializeDigits:(CGRect) frame
{
	NSLog(@"initDigits");
	int ow = config.width;
	int oh = config.height;

	config.width  = (int) frame.size.width;   // use the next-larger bitmap
	config.height = (int) frame.size.height;
	NSLog(@"fsw: %f, fsh: %f", frame.size.width, frame.size.height);
	
	NSLog(@"ch: %d, cw: %d", config.height, config.width);
	
	//  config.width = 1280;    // always use the biggest font image
	//  config.height = 1024;

	render_bitmap_size (&config, &config.width, &config.height);
NSLog(@"ch: %d, cw: %d", config.height, config.width);
	if (config.render_state && (ow == config.width && oh == config.height))
	 return;  // nothing to do

	// When the window is resized, re-create the bitmaps for the largest
	// font that will now fit in the window.
	//
	if (config.bitmap) free (config.bitmap);
	config.bitmap = calloc (1, config.height * (config.width << 3));
	if (! config.bitmap) abort();

	if (pixmap) free (pixmap);
	pixmap = calloc (1, config.height * config.width * 4);
	if (! pixmap) abort();

	if (config.render_state)
	 	render_free (&config);
	render_init (&config);
	NSLog(@"initDigits Done");
	NSLog(@"fsw: %f, fsh: %f", frame.size.width, frame.size.height);
	
	NSLog(@"ch: %d, cw: %d", config.height, config.width);
}

/* When this timer goes off, we re-pick the foreground/background colors,
   and mark the display as invalid.*/
 - (void)colorTick
{
	if (colorTimer && [colorTimer isValid]) {
		[colorTimer invalidate];
		colorTimer = 0;
	}

	float tick = 1.0 / 360.0;   // cycle H by one degree per tick

	fgh += tick;
	while (fgh > 1.0) fgh -= 1.0;
	
	unsigned short r, g, b;
	hsv_to_rgb (fgh * 360, fgs, fgv, &r, &g, &b);
	fgcolor = ((r << 11) + (g << 5) + b);
				
	bgh += tick * 0.91;   // cycle bg slightly slower than fg, for randomosity.
	while (bgh > 1.0) bgh -= 1.0;
	//NSLog(@"fg = %f,%f,%f bgh = %f,%f,%f", fgh, fgs, fgv, bgh, bgs, bgv);
	hsv_to_rgb (bgh * 360, bgs, bgv, &r, &g, &b);
    bgcolor = ((r << 11) + (g << 5) + b);

	//NSLog(@"fgcolor = %d, bgcolor = %d", fgcolor, bgcolor);
	//[self setForeground:fg2 background:bg2]; 

	/* re-schedule the timer according to current fps. */
	 
	float delay = 1.0 / config.max_cps;
	colorTimer = [NSTimer scheduledTimerWithTimeInterval:delay
	                                              target:self
	                                            selector:@selector(colorTick)
	                                            userInfo:nil
	                                             repeats:NO];
}

- (BOOL)ignoresMouseEvents {

    return NO;
}

- (void)mouseDown:(GSEvent *)event {
	CGPoint point = GSEventGetLocationInWindow(event);
	NSLog(@"mouseDown %f, %f", point.x, point.y);
	config.display_date_p = 1;
}

- (void)mouseUp:(GSEvent *)event {
	CGPoint point = GSEventGetLocationInWindow(event);
	NSLog(@"mouseUp %f, %f", point.x, point.y);
	config.display_date_p = 0;
}

- (void)deviceOrientationChanged:(int)screenOrientation {
	NSLog(@"Device orientation changed. From %d, to %d", __screenOrientation, screenOrientation);
	__screenOrientation = screenOrientation;
	/*    if (currentView != CUR_EMULATOR)
	        return;

	    rect = [ UIHardware fullScreenApplicationContentRect ];
	    rect.origin.x = rect.origin.y = 0.0f;

	    [ transitionView removeFromSuperview ];
	    [ transitionView release ];

	    EmulationView *newEmuView = [ self createEmulationView ];

	    transitionView = [ self createTransitionView ];
	    [ self addSubview: transitionView ];
	    [ transitionView transition:6 fromView:emuView toView:newEmuView ];

	    emuView = newEmuView;

	    if (__screenOrientation > 2) {
	        [ navBar removeFromSuperview ];
	    } else {
	        [ self addSubview: navBar ];
	        [ self setNavBar ];
	    }
	}*/
}

@end
