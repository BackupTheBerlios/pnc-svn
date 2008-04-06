#import <CoreFoundation/CoreFoundation.h>
#import <Foundation/Foundation.h>
#import <UIKit/CDStructures.h>
#import <UIKit/UIPushButton.h>
#import <UIKit/UIThreePartButton.h>
#import <UIKit/UINavigationBar.h>
#import <UIKit/UIWindow.h>
#import <UIKit/UIView-Hierarchy.h>
#import <UIKit/UIHardware.h>
#import <UIKit/UITable.h>
#import <UIKit/UITableCell.h>
#import <UIKit/UITableColumn.h>
#import "HelloApplication.h"

@implementation HelloApplication

- (int) numberOfRowsInTable: (UITable *)table
{
    return 2;
}

- (UITableCell *) table: (UITable *)table cellForRow: (int)row column: (int)col
{
    return row ? buttonCell : pbCell; 
}

- (UITableCell *) table: (UITable *)table cellForRow: (int)row column: (int)col
    reusing: (BOOL) reusing
{
    return pbCell; 
}

- (void) applicationDidFinishLaunching: (id) unused
{
    UIWindow *window;

    window = [[UIWindow alloc] initWithContentRect: [UIHardware
        fullScreenApplicationContentRect]];
   
    pbCell = [[UIImageAndTextTableCell alloc] init];
    [pbCell setTitle: @"Hello world!\n"]; 

    UIPushButton *button = [[UIThreePartButton alloc] initWithTitle:
        @"Touch Me"];
    buttonCell = [[UITableCell alloc] init];
    [buttonCell addSubview: button];
    [button sizeToFit];

    UITable *table = [[UITable alloc] initWithFrame: CGRectMake(0.0f, 48.0f,
        320.0f, 480.0f - 16.0f - 32.0f)];
    UITableColumn *col = [[UITableColumn alloc] initWithTitle: @"HelloApp"
        identifier: @"hello" width: 320.0f];

    [window orderFront: self];
    [window makeKey: self];
    [window _setHidden: NO];
 
    [table addTableColumn: col]; 
    [table setDataSource: self];
    [table setDelegate: self];
    [table reloadData];

    UINavigationBar *nav = [[UINavigationBar alloc] initWithFrame: CGRectMake(
        0.0f, 0.0f, 320.0f, 48.0f)];
    [nav showButtonsWithLeftTitle: @"Foo" rightTitle: @"Bar" leftBack: YES];
    [nav setBarStyle: 0];

    struct CGRect rect = [UIHardware fullScreenApplicationContentRect];
    rect.origin.x = rect.origin.y = 0.0f;
    UIView *mainView;
    mainView = [[UIView alloc] initWithFrame: rect];
    [mainView addSubview: nav]; 
    [mainView addSubview: table]; 

    [window setContentView: mainView]; 
}

@end