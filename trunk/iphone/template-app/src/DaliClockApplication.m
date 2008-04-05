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

#import <CoreFoundation/CoreFoundation.h>
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "DaliClockApplication.h"
#import "DaliClockView.h"
#import "logdebug.h"

char *progname = "Dali Clock";   // digital.c wants this for error messages.

@implementation DaliClockApplication

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification;
{
    UIWindow *window;

    window = [[UIWindow alloc] initWithContentRect: [UIHardware
        fullScreenApplicationContentRect]];
   
    [window orderFront: self];
    [window makeKey: self];
    [window _setHidden: NO];
 
    struct CGRect rect = [UIHardware fullScreenApplicationContentRect];
    rect.origin.x = rect.origin.y = 0.0f;
    DaliClockView *mainView;
    mainView = [[DaliClockView alloc] initWithFrame: rect];

    [window setContentView: mainView]; 
}

- (void)deviceOrientationChanged:(GSEvent *)event {
	LOGDEBUG("Device orientation changed");
    int newOrientation = [ UIHardware deviceOrientation: YES ];
	LOGDEBUG("new device orientation is %d", newOrientation);

    /* Only change orientation if we are post-initialized and in emulation
    if (screenOrientation == 0 || [ mainView isBrowsing ] == YES) {
        screenOrientation = newOrientation;
        return;
    }

    /* newOrientation > 2 : One of the landscape modes 
    /* orientation == 1   : Portrait mode 

    if ( ( newOrientation == 1 || newOrientation >2 ) &&
          screenOrientation != newOrientation )
    {
        IS_CHANGING_ORIENTATION = 1;
        if (newOrientation > 2) 
            [ UIHardware _setStatusBarHeight:0.0f ];
        else
            [ UIHardware _setStatusBarHeight:20.0f ];

        [ self setStatusBarMode: (newOrientation > 2) 
            ? (( [ mainView isBrowsing ] == YES) ? 0 : 2) : 0 duration: 0 ];

        LOGDEBUG("NESApp.deviceOrientationChanged(): Changing to %d", 
            newOrientation);
        UIWindow *newWindow = [ [ UIWindow alloc ] initWithContentRect:
                 [ UIHardware fullScreenApplicationContentRect ]
        ];

        [ newWindow setContentView: mainView ];
        [ newWindow orderFront: self ];
        [ newWindow makeKey: self ];
        [ newWindow _setHidden: NO ];
        [ window _setHidden: YES ];
        window = newWindow;
        screenOrientation = newOrientation;

        [ mainView deviceOrientationChanged: newOrientation ];
        IS_CHANGING_ORIENTATION = 0;
    }*/
} 

- (BOOL) respondsToSelector:(SEL)aSelector {
	NSString *methodName = NSStringFromSelector(aSelector);
	NSLog(@"respondsToSelector:%@", methodName);
	return [super respondsToSelector:aSelector];
}

@end