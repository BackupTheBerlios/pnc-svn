#include <string.h>
#include <stdio.h>
#include <stdarg.h>

#include "logdebug.h"

void LOGDEBUG(const char *text, ...)
{
  char debug_text[1024];
  va_list args;
  FILE *f;

  va_start (args, text);
  vsnprintf (debug_text, sizeof (debug_text), text, args);
  va_end (args);

  f = fopen("/tmp/daliclock.log", "a");
  fprintf(f, "%s\n", debug_text);
  fclose(f);
}