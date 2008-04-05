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
#import <GraphicsServices/GraphicsServices.h>

@interface DaliClockApplication : UIApplication {

}

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification;
- (void)deviceOrientationChanged:(GSEvent *)event;

@end