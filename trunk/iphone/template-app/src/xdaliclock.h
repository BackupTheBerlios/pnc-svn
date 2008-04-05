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

#ifndef __DALICLOCK_H__
#define __DALICLOCK_H__

extern char *progname;

struct render_state;

typedef struct {
  enum { HHMMSS, HHMM, SS }       time_mode;
  enum { MMDDYY, DDMMYY, YYMMDD } date_mode;
  int display_date_p;			     /* displaying time, or date */
  int twelve_hour_p;			     /* 12 hour time vs 24 hour time */
/*  int cycle_p;	*/			     /* color cycling */
/*  int transparent_p;	*/		     /* shaped window */
  time_t countdown;			     /* countdown to/from here */
  char test_hack;			     /* display this character once */

  int max_fps;				     /* desired frames per second */
  int max_cps;				     /* color cycle ticks per second */
  struct render_state *render_state;         /* internal to digital.c */
  unsigned char *bitmap;		     /* rendered output */
  unsigned int width, height;		     /* size of bitmap */

} dali_config;

extern void render_init (dali_config *);
extern void render_free (dali_config *);
extern void render_once (dali_config *,
                         unsigned long time,
                         unsigned long usecs);
extern void render_bitmap_size (dali_config *c,
                                unsigned int *w_ret, unsigned int *h_ret);

#endif /* __DALICLOCK_H__ */
