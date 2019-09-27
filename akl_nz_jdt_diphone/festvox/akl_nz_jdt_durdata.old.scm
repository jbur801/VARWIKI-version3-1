;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;                                                                       ;;
;;;                Centre for Speech Technology Research                  ;;
;;;                     University of Edinburgh, UK                       ;;
;;;                       Copyright (c) 1996,1997                         ;;
;;;                        All Rights Reserved.                           ;;
;;;                                                                       ;;
;;;  Permission to use, copy, modify, distribute this software and its    ;;
;;;  documentation for research, educational and individual use only, is  ;;
;;;  hereby granted without fee, subject to the following conditions:     ;;
;;;   1. The code must retain the above copyright notice, this list of    ;;
;;;      conditions and the following disclaimer.                         ;;
;;;   2. Any modifications must be clearly marked as such.                ;;
;;;   3. Original authors' names are not deleted.                         ;;
;;;  This software may not be used for commercial purposes without        ;;
;;;  specific prior written permission from the authors.                  ;;
;;;                                                                       ;;
;;;  THE UNIVERSITY OF EDINBURGH AND THE CONTRIBUTORS TO THIS WORK        ;;
;;;  DISCLAIM ALL WARRANTIES WITH REGARD TO THIS SOFTWARE, INCLUDING      ;;
;;;  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO EVENT   ;;
;;;  SHALL THE UNIVERSITY OF EDINBURGH NOR THE CONTRIBUTORS BE LIABLE     ;;
;;;  FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES    ;;
;;;  WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN   ;;
;;;  AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,          ;;
;;;  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF       ;;
;;;  THIS SOFTWARE.                                                       ;;
;;;                                                                       ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;;   A hand specified tree to predict zcore durations
;;;
;;;

(set! akl_nz_jdt::zdur_tree 
 '
   ((R:SylStructure.parent.R:Syllable.p.syl_break > 1 ) ;; clause initial
    ((1.5))
    ((R:SylStructure.parent.syl_break > 1)   ;; clause final
     ((1.5))
     ((1.0)))))

(set! akl_nz_jdt::phone_durs
'(
  ;;; PHONE DATA  
  ;; name zero mean in seconds e.g.
  (# 0.0 0.250)
  (uh 0.0 0.160)
   (e   0.0 0.080)
   (a   0.0 0.120)
   (o   0.0 0.100)
   (u  0.0 0.120)
   (i   0.0 0.150)
   (ii  0.0 0.200)
   (ir  0.0 0.250)
   (uu  0.0 0.170)
   (uw  0.0 0.170)
   (oo  0.0 0.200)
   (aa  0.0 0.180)
   (@@r 0.0 0.200)
   (ai  0.0 0.150)
   (ei  0.0 0.150)
   (oi  0.0 0.300)
   (ow  0.0 0.150)
   (ou  0.0 0.180)
   (oul 0.0 0.080)
   (@  0.0 0.050)
   (p 0.0 0.100)
   (t 0.0 0.080)
   (t^ 0.0 0.050)
   (k  0.0 0.080)
   (b  0.0 0.080)
   (d  0.0 0.050)
   (g 0.0 0.080)
   (s  0.0 0.100)
   (z 0.0 0.080)
   (sh 0.0 0.120)
   (zh 0.0 0.080)
   (f 0.0 0.100)
   (v 0.0 0.100)
   (th 0.0 0.100)
   (dh 0.0 0.080)
   (ch  0.0 0.100)
   (jh 0.0 0.050)
   (h   0.0 0.050)
   (m 0.0 0.100)
   (m! 0.0 0.180)
   (n 0.0 0.100)
   (n! 0.0 0.100)
   (ng 0.0 0.100)
   (l  0.0 0.100)
   (l! 0.0 0.150)
   (y 0.0 0.100)
   (r 0.0 0.080)
   (w 0.0 0.100)
))

(provide 'akl_nz_jdt_durdata)
