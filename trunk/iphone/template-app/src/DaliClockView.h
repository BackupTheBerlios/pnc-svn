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

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <CoreSurface/CoreSurface.h>
#import <LayerKit/LKLayer.h>
#import "xdaliclock.h"
#include <pthread.h>

CoreSurfaceBufferRef screenSurface;
pthread_cond_t screenUpdateLock;
pthread_mutex_t screenUpdateMutex;
extern int __screenOrientation;

@interface DaliClockView : UIView 
{
    LKLayer *screenLayer;
	int initGraphics;
	
	unsigned short color;	
	dali_config config;
  	unsigned char *pixmap;
  	NSTimer *clockTimer;
	NSTimer *colorTimer;
	
	float fgh, fgs, fgv, bgh, bgs, bgv;
	unsigned short fgcolor, bgcolor;
  
}

- (id) initWithFrame:(CGRect)frame;
- (void) dealloc;
- (void) drawRect:(CGRect)frame;
- (void) updateScreen;

- (void) intializeDigits:(CGRect) frame;
- (void) clockTick;
- (void) colorTick;

- (void)deviceOrientationChanged:(int)orientation;

@end
