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

#ifdef HAVE_CONFIG_H
# include "config.h"
#endif

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <sys/time.h>
#include <time.h>

#include "xdaliclock.h"

typedef unsigned short POS;
typedef unsigned char BOOL;

#ifdef BUILTIN_FONTS

/* static int use_builtin_font; */

struct raw_number {
  const unsigned char *bits;
  POS width, height;
};

#endif /* BUILTIN_FONTS */

#ifdef BUILTIN_FONTS

# include "zeroB.xbm"
# include "oneB.xbm"
# include "twoB.xbm"
# include "threeB.xbm"
# include "fourB.xbm"
# include "fiveB.xbm"
# include "sixB.xbm"
# include "sevenB.xbm"
# include "eightB.xbm"
# include "nineB.xbm"
# include "colonB.xbm"
# include "slashB.xbm"

static struct raw_number numbers_B [] = {
  { zeroB_bits,  zeroB_width,  zeroB_height  },
  { oneB_bits,   oneB_width,   oneB_height   },
  { twoB_bits,   twoB_width,   twoB_height   },
  { threeB_bits, threeB_width, threeB_height },
  { fourB_bits,  fourB_width,  fourB_height  },
  { fiveB_bits,  fiveB_width,  fiveB_height  },
  { sixB_bits,   sixB_width,   sixB_height   },
  { sevenB_bits, sevenB_width, sevenB_height },
  { eightB_bits, eightB_width, eightB_height },
  { nineB_bits,  nineB_width,  nineB_height  },
  { colonB_bits, colonB_width, colonB_height },
  { slashB_bits, slashB_width, slashB_height },
  { 0, }
};

# include "zeroC.xbm"
# include "oneC.xbm"
# include "twoC.xbm"
# include "threeC.xbm"
# include "fourC.xbm"
# include "fiveC.xbm"
# include "sixC.xbm"
# include "sevenC.xbm"
# include "eightC.xbm"
# include "nineC.xbm"
# include "colonC.xbm"
# include "slashC.xbm"

static struct raw_number numbers_C [] = {
  { zeroC_bits,  zeroC_width,  zeroC_height  },
  { oneC_bits,   oneC_width,   oneC_height   },
  { twoC_bits,   twoC_width,   twoC_height   },
  { threeC_bits, threeC_width, threeC_height },
  { fourC_bits,  fourC_width,  fourC_height  },
  { fiveC_bits,  fiveC_width,  fiveC_height  },
  { sixC_bits,   sixC_width,   sixC_height   },
  { sevenC_bits, sevenC_width, sevenC_height },
  { eightC_bits, eightC_width, eightC_height },
  { nineC_bits,  nineC_width,  nineC_height  },
  { colonC_bits, colonC_width, colonC_height },
  { slashC_bits, slashC_width, slashC_height },
  { 0, }
};

# include "zeroD.xbm"
# include "oneD.xbm"
# include "twoD.xbm"
# include "threeD.xbm"
# include "fourD.xbm"
# include "fiveD.xbm"
# include "sixD.xbm"
# include "sevenD.xbm"
# include "eightD.xbm"
# include "nineD.xbm"
# include "colonD.xbm"
# include "slashD.xbm"

static struct raw_number numbers_D [] = {
  { zeroD_bits,  zeroD_width,  zeroD_height  },
  { oneD_bits,   oneD_width,   oneD_height   },
  { twoD_bits,   twoD_width,   twoD_height   },
  { threeD_bits, threeD_width, threeD_height },
  { fourD_bits,  fourD_width,  fourD_height  },
  { fiveD_bits,  fiveD_width,  fiveD_height  },
  { sixD_bits,   sixD_width,   sixD_height   },
  { sevenD_bits, sevenD_width, sevenD_height },
  { eightD_bits, eightD_width, eightD_height },
  { nineD_bits,  nineD_width,  nineD_height  },
  { colonD_bits, colonD_width, colonD_height },
  { slashD_bits, slashD_width, slashD_height },
  { 0, }
};

# include "zeroE.xbm"
# include "oneE.xbm"
# include "twoE.xbm"
# include "threeE.xbm"
# include "fourE.xbm"
# include "fiveE.xbm"
# include "sixE.xbm"
# include "sevenE.xbm"
# include "eightE.xbm"
# include "nineE.xbm"
# include "colonE.xbm"
# include "slashE.xbm"

static struct raw_number numbers_E [] = {
  { zeroE_bits,  zeroE_width,  zeroE_height  },
  { oneE_bits,   oneE_width,   oneE_height   },
  { twoE_bits,   twoE_width,   twoE_height   },
  { threeE_bits, threeE_width, threeE_height },
  { fourE_bits,  fourE_width,  fourE_height  },
  { fiveE_bits,  fiveE_width,  fiveE_height  },
  { sixE_bits,   sixE_width,   sixE_height   },
  { sevenE_bits, sevenE_width, sevenE_height },
  { eightE_bits, eightE_width, eightE_height },
  { nineE_bits,  nineE_width,  nineE_height  },
  { colonE_bits, colonE_width, colonE_height },
  { slashE_bits, slashE_width, slashE_height },
  { 0, }
};

# include "zeroF.xbm"
# include "oneF.xbm"
# include "twoF.xbm"
# include "threeF.xbm"
# include "fourF.xbm"
# include "fiveF.xbm"
# include "sixF.xbm"
# include "sevenF.xbm"
# include "eightF.xbm"
# include "nineF.xbm"
# include "colonF.xbm"
# include "slashF.xbm"

static struct raw_number numbers_F [] = {
  { zeroF_bits,  zeroF_width,  zeroF_height  },
  { oneF_bits,   oneF_width,   oneF_height   },
  { twoF_bits,   twoF_width,   twoF_height   },
  { threeF_bits, threeF_width, threeF_height },
  { fourF_bits,  fourF_width,  fourF_height  },
  { fiveF_bits,  fiveF_width,  fiveF_height  },
  { sixF_bits,   sixF_width,   sixF_height   },
  { sevenF_bits, sevenF_width, sevenF_height },
  { eightF_bits, eightF_width, eightF_height },
  { nineF_bits,  nineF_width,  nineF_height  },
  { colonF_bits, colonF_width, colonF_height },
  { slashF_bits, slashF_width, slashF_height },
  { 0, }
};

#endif /* BUILTIN_FONTS */

#undef countof
#define countof(x) (sizeof((x))/sizeof(*(x)))

/* Number of horizontal segments/line.  Enlarge this if you are trying
   to use a font that is too "curvy" for XDaliClock to cope with.
   This code was sent to me by Dan Wallach <c169-bg@auriga.berkeley.edu>.
   I'm highly opposed to ever using statically-sized arrays, but I don't
   really feel like hacking on this code enough to clean it up.
 */
#ifndef MAX_SEGS_PER_LINE
# define MAX_SEGS_PER_LINE 5
#endif

struct scanline {
  POS left[MAX_SEGS_PER_LINE], right[MAX_SEGS_PER_LINE];
};

struct frame {
  struct scanline scanlines [1]; /* scanlines are contiguous here */
};


/* The runtime settings (some initialized from system prefs, but changable.)
 */
struct render_state {

  enum date_state { DTime, DDateIn, DDate, DDateOut, DDateOut2, DDash, DDash2 }
    display_date;

  unsigned int last_time;

  int char_width, char_height, colon_width;

  struct frame *base_frames [12];
  struct frame *orig_frames [6];
  struct frame *current_frames [6];
  struct frame *target_frames [6];
  struct frame *clear_frame;

  signed char current_digits [6];
  signed char target_digits [6];
  BOOL digits_clean_p [6];     /* "clean" means "not stuck between digits" */
};


static struct frame *
make_blank_frame (int width, int height)
{
  int size = sizeof (struct frame) + (sizeof (struct scanline) * (height - 1));
  struct frame *frame;
  int x, y;

  frame = (struct frame *) calloc (size, 1);
  for (y = 0; y < height; y++)
    for (x = 0; x < MAX_SEGS_PER_LINE; x++)
      frame->scanlines[y].left [x] = frame->scanlines[y].right [x] = width / 2;
  return frame;
}


static struct frame *
number_to_frame (const unsigned char *bits, int width, int height)
{
  int x, y;
  struct frame *frame;
  POS *left, *right;

  frame = make_blank_frame (width, height);

  for (y = 0; y < height; y++)
    {
      int seg, end;
      x = 0;
# define GETBIT(bits,x,y) \
         (!! ((bits) [((y) * ((width+7) >> 3)) + ((x) >> 3)] \
              & (1 << ((x) & 7))))

      left = frame->scanlines[y].left;
      right = frame->scanlines[y].right;

      for (seg = 0; seg < MAX_SEGS_PER_LINE; seg++)
        left [seg] = right [seg] = width / 2;

      for (seg = 0; seg < MAX_SEGS_PER_LINE; seg++)
        {
          for (; x < width; x++)
            if (GETBIT (bits, x, y)) break;
          if (x == width) break;
          left [seg] = x;
          for (; x < width; x++)
            if (! GETBIT (bits, x, y)) break;
          right [seg] = x;
        }

      for (; x < width; x++)
        if (GETBIT (bits, x, y))
          {
            /* This means the font is too curvy.  Increase MAX_SEGS_PER_LINE
               and recompile. */
            fprintf (stderr, "%s: font is too curvy\n", progname);
            exit (-1);
          }

      /* If there were any segments on this line, then replicate the last
         one out to the end of the line.  If it's blank, leave it alone,
         meaning it will be a 0-pixel-wide line down the middle.
       */
      end = seg;
      if (end > 0)
        for (; seg < MAX_SEGS_PER_LINE; seg++)
          {
            left [seg] = left [end-1];
            right [seg] = right [end-1];
          }

# undef GETBIT
    }

  return frame;
}


static void
copy_frame (dali_config *c, struct frame *from, struct frame *to)
{
  struct render_state *state = c->render_state;
  int y;
  for (y = 0; y < state->char_height; y++)
    to->scanlines[y] = from->scanlines[y];  /* copies the whole struct */
}


static BOOL
frame_equal (dali_config *c, struct frame *a, struct frame *b)
{
  struct render_state *state = c->render_state;
  int y;
  for (y = 0; y < state->char_height; y++)
    /* compare the whole scanline struct at once */
    if (memcmp (a->scanlines, b->scanlines, sizeof(b->scanlines)))
      return 0;
  return 1;
}


static int
pick_font_size (dali_config *c, unsigned int *w_ret, unsigned int *h_ret)
{
#ifdef BUILTIN_FONTS
  int nn, cc;
  int f;
  unsigned int w, h;
  unsigned int ww = (w_ret ? *w_ret : c->width);
  unsigned int hh = (h_ret ? *h_ret : c->height);

  switch (c->time_mode)
    {
    case SS:     nn = 2, cc = 0; break;
    case HHMM:   nn = 4, cc = 1; break;
    case HHMMSS: nn = 6, cc = 2; break;
    default:   abort(); break;
    }

  if      ((w = ((numbers_B[0].width * nn) +
                 (numbers_B[10].width * cc))) <= ww &&
           ((h = numbers_B[0].height) <= hh))
    f = 4;
  else if ((w = ((numbers_C[0].width * nn) +
                 (numbers_C[10].width * cc))) <= ww &&
           ((h = numbers_C[0].height) <= hh))
    f = 3;
  else if ((w = ((numbers_D[0].width * nn) +
                 (numbers_D[10].width * cc))) <= ww &&
           ((h = numbers_D[0].height) <= hh))
    f = 2;
  else if ((w = ((numbers_E[0].width * nn) +
                 (numbers_E[10].width * cc))) <= ww &&
           ((h = numbers_E[0].height) <= hh))
    f = 1;
  else
    {
      w = ((numbers_F[0].width * nn) +
           (numbers_F[10].width * cc));
      h = numbers_F[0].height;
      f = 0;
    }

  w = ((w + 7) / 8) * 8;  /* round up to byte */

#else  /* !BUILTIN_FONTS */
  int w = 0, h = 0, f = 0;
#endif /* !BUILTIN_FONTS */
  
  if (w_ret) *w_ret = w;
  if (h_ret) *h_ret = h;
  return f;
}


static void
init_numbers (dali_config *c)
{
  struct render_state *state = c->render_state;
  int i;
#ifdef BUILTIN_FONTS
  struct raw_number *raw;

  int size = pick_font_size (c, NULL, NULL);
  switch (size)
    {
    case 0: raw = numbers_F; break;
    case 1: raw = numbers_E; break;
    case 2: raw = numbers_D; break;
    case 3: raw = numbers_C; break;
    case 4: raw = numbers_B; break;
    default: abort(); break;
    }

  state->char_width  = raw[0].width;
  state->char_height = raw[0].height;
  state->colon_width = raw[10].width;

  for (i = 0; i < countof(state->base_frames); i++)
    state->base_frames [i] =
      number_to_frame (raw[i].bits, raw[i].width, raw[i].height);
#endif /* BUILTIN_FONTS */

  for (i = 0; i < countof(state->orig_frames); i++)
    state->orig_frames [i] =
      make_blank_frame (state->char_width, state->char_height);

  for (i = 0; i < countof(state->current_frames); i++)
    state->current_frames [i] =
      make_blank_frame (state->char_width, state->char_height);

  for (i = 0; i < countof(state->target_frames); i++)
    state->target_frames [i] =
      make_blank_frame (state->char_width, state->char_height);

  state->clear_frame = make_blank_frame (state->char_width,
                                         state->char_height);

  for (i = 0; i < countof(state->target_digits); i++)
    state->target_digits[i] = state->current_digits[i] = -1;

  memset (state->digits_clean_p, 0, sizeof(state->digits_clean_p));

  memset (c->bitmap, 0, c->height * (c->width >> 3));
}


static void
free_numbers (dali_config *c)
{
  struct render_state *state = c->render_state;
  int i;
# define FREEIF(x) do { if ((x)) { free((x)); (x) = 0; } } while (0)
# define FREELOOP(x) do { \
    for (i = 0; i < countof ((x)); i++) FREEIF ((x)[i]); } while (0)

  FREELOOP (state->base_frames);
  FREELOOP (state->orig_frames);
  FREELOOP (state->current_frames);
  FREELOOP (state->target_frames);
  FREEIF (state->clear_frame);

# undef FREELOOP
# undef FREEIF
}


static void
fill_target_digits (dali_config *c, unsigned long time)
{
  struct render_state *state = c->render_state;
  struct tm *tm = localtime ((time_t *) &time);

  if (c->test_hack)
    {
      state->target_digits [0] =
        state->target_digits [1] =
        state->target_digits [2] =
        state->target_digits [3] =
        state->target_digits [4] =
        state->target_digits [5] = (c->test_hack == '-' ? -1
                                    : c->test_hack - '0');
      c->test_hack = 0;
    }
  else if (state->display_date == DTime ||
           state->display_date == DDash ||
           state->display_date == DDash2)
    {
      BOOL twelve_hour_time_p = c->twelve_hour_p;

      if (state->display_date == DDash)
        state->display_date = DDash2;
      else if (state->display_date == DDash2)
        state->display_date = DTime;

      if (c->countdown)
        {
          long delta = ((unsigned long) c->countdown) - time;
          if (delta < 0) delta = -delta;
          tm->tm_sec = delta % 60;
          tm->tm_min = (delta / 60) % 60;
          tm->tm_hour = (delta / (60 * 60)) % 100;
          twelve_hour_time_p = 0;
        }
      if (twelve_hour_time_p && tm->tm_hour > 12) tm->tm_hour -= 12;
      if (twelve_hour_time_p && tm->tm_hour == 0) tm->tm_hour = 12;
      state->target_digits [0] = (tm->tm_hour - (tm->tm_hour % 10)) / 10;
      state->target_digits [1] = tm->tm_hour % 10;
      state->target_digits [2] = (tm->tm_min - (tm->tm_min % 10)) / 10;
      state->target_digits [3] = tm->tm_min % 10;
      state->target_digits [4] = (tm->tm_sec - (tm->tm_sec % 10)) / 10;
      state->target_digits [5] = tm->tm_sec % 10;

      if (twelve_hour_time_p && state->target_digits [0] == 0)
        state->target_digits [0] = -1;
    }
  else
    {
      int m0,m1,d0,d1,y0,y1;
      tm->tm_mon++; /* 0 based */
      m0 = (tm->tm_mon - (tm->tm_mon % 10)) / 10;
      m1 = tm->tm_mon % 10;
      d0 = (tm->tm_mday - (tm->tm_mday % 10)) / 10;
      d1 = tm->tm_mday % 10;
      y0 = tm->tm_year % 100;
      y0 = (y0 - (y0 % 10)) / 10;
      y1 = tm->tm_year % 10;

      if (state->display_date == DDateIn)
        state->display_date = DDate;
      if (state->display_date == DDateOut)
        state->display_date = DDateOut2;
      else if (state->display_date == DDateOut2)
        state->display_date = DDash;
      else if (state->display_date == DDash)
        state->display_date = DDash2;

      switch (c->date_mode)
        {
        case MMDDYY:
          switch (c->time_mode)
            {
            case HHMMSS:
            case HHMM:
              state->target_digits [0] = m0; state->target_digits [1] = m1;
              state->target_digits [2] = d0; state->target_digits [3] = d1;
              state->target_digits [4] = y0; state->target_digits [5] = y1;
              break;
            case SS:
              state->target_digits [4] = d0; state->target_digits [5] = d1;
              break;
            default:
              abort();
            }
          break;
        case DDMMYY:
          switch (c->time_mode)
            {
            case HHMMSS:
            case HHMM:
              state->target_digits [0] = d0; state->target_digits [1] = d1;
              state->target_digits [2] = m0; state->target_digits [3] = m1;
              state->target_digits [4] = y0; state->target_digits [5] = y1;
              break;
            case SS:
              state->target_digits [4] = d0; state->target_digits [5] = d1;
              break;
            default:
              abort();
            }
          break;
        case YYMMDD:
          switch (c->time_mode)
            {
            case HHMMSS:
            case SS:
              state->target_digits [0] = y0; state->target_digits [1] = y1;
              state->target_digits [2] = m0; state->target_digits [3] = m1;
              state->target_digits [4] = d0; state->target_digits [5] = d1;
              break;
            case HHMM:
              state->target_digits [0] = m0; state->target_digits [1] = m1;
              state->target_digits [2] = d0; state->target_digits [3] = d1;
              break;
            default:
              abort();
            }
          break;              
        default:
          abort();
          break;
        }
    }
}


static void
draw_horizontal_line (dali_config *c, int x1, int x2, int y, BOOL black_p)
{
  unsigned char *scanline;
  if (x1 == x2) return;
  if (y > c->height) return;
  if (x1 > c->width) x1 = c->width;
  if (x2 > c->width) x2 = c->width;
  if (x1 > x2)
    {
      int swap = x1;
      x1 = x2;
      x2 = swap;
    }

  scanline = c->bitmap + (y * (c->width >> 3));
  
#define BIGENDIAN
  
#ifdef BIGENDIAN
  if (black_p)
    for (; x1 < x2; x1++)
      scanline[x1>>3] |= 1 << (7 - (x1 & 7));
  else
    for (; x1 < x2; x1++)
      scanline[x1>>3] &= ~(1 << (7 - (x1 & 7)));
#else  /* !BIGENDIAN */
  if (black_p)
    for (; x1 < x2; x1++)
      scanline[x1>>3] |= 1 << (x1 & 7);
  else
    for (; x1 < x2; x1++)
      scanline[x1>>3] &= ~(1 << (x1 & 7));
#endif /* !BIGENDIAN */
}


static void
draw_frame (dali_config *c, struct frame *frame, int x, int y, int colonic_p)
{
  struct render_state *state = c->render_state;
  int px, py;
  int cw = (colonic_p ? state->colon_width : state->char_width);

  for (py = 0; py < state->char_height; py++)
    {
      struct scanline *line = &frame->scanlines [py];
      int last_right = 0;

      for (px = 0; px < MAX_SEGS_PER_LINE; px++)
        {
          if (px > 0 &&
              (line->left[px] == line->right[px] ||
               (line->left [px] == line->left [px-1] &&
                line->right[px] == line->right[px-1])))
            continue;

          /* Erase the line between the last segment and this segment.
           */
          draw_horizontal_line (c,
                                x + last_right,
                                x + line->left [px],
                                y + py,
                                0);

          /* Draw the line of this segment.
           */
          draw_horizontal_line (c,
                                x + line->left [px],
                                x + line->right[px],
                                y + py,
                                1);

          last_right = line->right[px];
        }

      /* Erase the line between the last segment and the right edge.
       */
      draw_horizontal_line (c,
                            x + last_right,
                            x + cw,
                            y + py,
                            0);
    }
}


static void draw_clock (dali_config *c, BOOL colonic_p);

static void
start_sequence (dali_config *c, unsigned long time)
{
  struct render_state *state = c->render_state;
  int i;

  /* Copy the (old) current_frames into the (new) orig_frames,
     since that's what's on the screen now. */
  for (i = 0; i < countof (state->current_frames); i++)
    copy_frame (c, state->current_frames[i], state->orig_frames[i]);

  /* likewise with the (old) current_digits */
  memcpy (state->current_digits, state->target_digits,
          sizeof (state->target_digits));

  /* generate new target_digits */
  fill_target_digits (c, time);

  /* Fill the (new) target_frames from the (new) target_digits. */
  for (i = 0; i < countof (state->target_frames); i++)
    {
      int j = state->target_digits[i];
      struct frame *to = state->target_frames[i];
      struct frame *from = (j < 0 
                            ? state->clear_frame
                            : state->base_frames [j]);
      copy_frame (c, from, to);
      
      /* This digit is "clean" if what is currently on screen is exactly
         equal to the rest-state digit (not in some half-animated inbetween
         state, as it might be if the wall clock moved from 4.8 seconds
         directly to 5.2 seconds without stopping closer to 5.0.) */
      state->digits_clean_p[i] = frame_equal (c, to, state->current_frames[i]);
    }

  /* Render the current frame. */
  draw_clock (c, 1);
}


static void
one_step (dali_config *c,
          struct frame *orig_frame,
          struct frame *current_frame,
          struct frame *target_frame,
          unsigned int msecs)
{
  struct render_state *state = c->render_state;
  struct scanline *orig   =    &orig_frame->scanlines [0];
  struct scanline *curr   = &current_frame->scanlines [0];
  struct scanline *target =  &target_frame->scanlines [0];
  int i = 0, x;

  for (i = 0; i < state->char_height; i++)
    {
# define STEP(field) \
         (curr->field = (orig->field \
                         + (((int) (target->field - orig->field)) \
                            * (int) msecs / 1000)))

      for (x = 0; x < MAX_SEGS_PER_LINE; x++)
        {
          STEP (left [x]);
          STEP (right[x]);
        }
      orig++;
      curr++;
      target++;
# undef STEP
    }
}


static void
tick_sequence (dali_config *c, unsigned long time, unsigned long msecs)
{
  struct render_state *state = c->render_state;
  int i;

  if (time != state->last_time)
    {
      /* End of the animation sequence; fill target_frames with the
         digits of the current time. */
      start_sequence (c, time);
      state->last_time = time;
/*      return; #### */
    }

  /* Linger for about 1/10th second at the end of each cycle. */
  msecs *= 1.2;
  if (msecs > 1000) msecs = 1000;

  /* Construct current_frames by interpolating between
     orig_frames and target_frames. */
  for (i = 0; i < countof (state->current_frames); i++)
    /* We can skip animating this frame if:
       - the old and new digit are the same (no motion desired); and
       - what is on the screen is a rest-state of that digit (not an
         intermediate frame of the last animation that didn't fully
         complete). */
    if (state->target_digits[i] != state->current_digits[i] ||
        !state->digits_clean_p[i])
      {
        one_step (c,
                  state->orig_frames[i],
                  state->current_frames[i],
                  state->target_frames[i],
                  msecs);
        state->digits_clean_p[i] = 0; /* we just animated; no longer clean */
      }

}


static void
draw_clock (dali_config *c, BOOL colonic_p)
{
  struct render_state *state = c->render_state;
  int x, y;
  int width = c->width;
  int height = c->height;
  int nn, cc;

  switch (c->time_mode)
    {
    case SS:     nn = 2, cc = 0; break;
    case HHMM:   nn = 4, cc = 1; break;
    case HHMMSS: nn = 6, cc = 2; break;
    default:   abort(); break;
    }

  x = (width - ((state->char_width * nn) +
                (state->colon_width * cc))) / 2;
  y = (height - state->char_height) / 2;

/* As above: 
    We can skip rendering this frame if:
    - the old and new digit are the same (no motion desired); and
    - what is on the screen is a rest-state of that digit (not an
      intermediate frame of the last animation that didn't fully
      complete). */
# define DIGIT(n) \
   if (state->target_digits[n] != state->current_digits[n] || \
      !state->digits_clean_p[n]) \
     draw_frame (c, state->current_frames [n], x, y, 0); \
  x += state->char_width

# define COLON() \
  if (colonic_p) \
    draw_frame (c, state->base_frames \
                         [state->display_date == DTime ? 10 : 11], \
                x, y, 1); \
  x += state->colon_width

  switch (c->time_mode)
    {
    case SS:
      DIGIT(4);
      DIGIT(5);
      break;
    case HHMM:
      DIGIT(0);
      DIGIT(1);
      COLON();
      DIGIT(2);
      DIGIT(3);
      break;
    case HHMMSS:
      DIGIT(0);
      DIGIT(1);
      COLON();
      DIGIT(2);
      DIGIT(3);
      COLON();
      DIGIT(4);
      DIGIT(5);
      break;
    default:
      abort();
      break;
    }
# undef COLON
# undef DIGIT
}


void
render_init (dali_config *c)
{
  if (c->render_state) abort();
  c->render_state = (struct render_state *)
    calloc (1, sizeof (struct render_state));
  c->render_state->display_date = DTime;
  init_numbers (c);
}

void
render_free (dali_config *c)
{
  if (!c->render_state) abort();
  free_numbers (c);
  free (c->render_state);
  c->render_state = 0;
}

void
render_once (dali_config *c, unsigned long time, unsigned long usecs)
{
  struct render_state *state = c->render_state;

  if (c->display_date_p && state->display_date == DTime)
    state->display_date = DDateIn;
  else if (!c->display_date_p && state->display_date == DDate)
    state->display_date = DDateOut;

  if (! c->render_state) abort();
  if (! c->bitmap) abort();
  tick_sequence (c, time, (usecs / 1000));
  draw_clock (c, 0);
}

void
render_bitmap_size (dali_config *c, unsigned int *w_ret, unsigned int *h_ret)
{
  pick_font_size (c, w_ret, h_ret);
}
