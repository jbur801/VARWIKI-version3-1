;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;-*-mode:scheme-*-
;;;                                                                       ;;
;;;                    Jonathan Teutenberg                                ;;
;;;                         Copyright (c) 2000                            ;;
;;;                        All Rights Reserved.                           ;;
;;;                                                                       ;;
;;;  Distribution policy                                                  ;;                                                 ;;
;;;     Free for any use                                                  ;;
;;;                                                                       ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;;  An example Adapted from the jdt voice
;;;
;;;  Authors: Jonathan Teutenberg, Catherine Watson, Amelie Marchi
;;;

;;; Try to find out where we are
(if (assoc 'akl_nz_cw_cg_diphone voice-locations)
    (defvar akl_nz_cw_cg_dir 
      (cdr (assoc 'akl_nz_cw_cg_diphone voice-locations)))
    ;;; Not installed in Festival yet so assume running in place
    (defvar akl_nz_cw_cg_dir (pwd)))

(if (not (probe_file (path-append akl_nz_cw_cg_dir "festvox/")))
    (begin
     (format stderr "akl_nz_cw_cg: Can't find voice scm files they are not in\n")
     (format stderr "   %s\n" (path-append akl_nz_cw_cg_dir "festvox/"))
     (format stderr "   Either the voice isn't linked into Festival\n")
     (format stderr "   or you are starting festival in the wrong directory\n")
     (error)))

;;;  Add the directory contains general voice stuff to load-path
(set! load-path (cons (path-append akl_nz_cw_cg_dir "festvox/") load-path))

;;; Voice specific parameter are defined in each of the following
;;; files
(require 'akl_nz_cw_cg_phoneset)
(require 'akl_nz_cw_cg_tokenizer)
(require 'akl_nz_cw_cg_tagger)
(require 'akl_nz_cw_cg_lexicon)
(require 'akl_nz_cw_cg_phrasing)
(require 'akl_nz_cw_cg_intonation)
(require 'akl_nz_cw_cg_duration)
(require 'akl_nz_cw_cg_f0model)
(require 'akl_nz_cw_cg_other)
;; ... and others as required

;;;  Ensure we have a festival with the right diphone support compiled in
(require_module 'UniSyn)

(set! akl_nz_cw_cg_lpc_sep 
      (list
       '(name "akl_nz_cw_cg_lpc_sep")
       (list 'index_file (path-append akl_nz_cw_cg_dir "dic/cw2diph.est"))
       '(grouped "false")
       (list 'coef_dir (path-append akl_nz_cw_cg_dir "lpc"))
       (list 'sig_dir  (path-append akl_nz_cw_cg_dir "lpc"))
       '(coef_ext ".lpc")
       '(sig_ext ".res")
       (list 'default_diphone 
	     (string-append
	      (car (cadr (car (PhoneSet.description '(silences)))))
	      "-"
	      (car (cadr (car (PhoneSet.description '(silences)))))))))

(set! akl_nz_cw_cg_lpc_group 
      (list
       '(name "cw2_lpc_group")
       (list 'index_file 
	     (path-append akl_nz_cw_cg_dir "group/cw2lpc.group"))
       '(grouped "true")
       (list 'default_diphone 
	     (string-append
	      (car (cadr (car (PhoneSet.description '(silences)))))
	      "-"
	      (car (cadr (car (PhoneSet.description '(silences)))))))))

;; Go ahead and set up the diphone db
(set! akl_nz_cw_cg_db_name (us_diphone_init akl_nz_cw_cg_lpc_sep))
;; Once you've built the group file you can comment out the above and
;; uncomment the following.
;(set! akl_nz_cw_cg_db_name (us_diphone_init akl_nz_cw_cg_lpc_group))

(define (akl_nz_cw_cg_diphone_fix utt)
"(akl_nz_cw_cg_diphone_fix UTT)
Map phones to phonological variants if the diphone database supports
them."
  (mapcar
   (lambda (s)
     (let ((name (item.name s)))
       (akl_nz_cw_cg_diphone_fix_phone_name utt s)
       ))
   (utt.relation.items utt 'Segment))
  utt)

(define (akl_nz_cw_cg_diphone_fix_phone_name utt seg)
"(akl_nz_cw_cg_fix_phone_name UTT SEG)
Add the feature diphone_phone_name to given segment with the appropriate
name for constructing a diphone.  Basically adds _ if either side is part
of the same consonant cluster, adds $ either side if in different
syllable for preceding/succeeding vowel syllable."
  (let ((name (item.name seg)))
    (cond
     ((string-equal name "pau") t)
     ((string-equal "-" (item.feat seg 'ph_vc))
      (if (and (member_string name '(r w y l))
	       (member_string (item.feat seg "p.name") '(p t k b d g))
	       (item.relation.prev seg "SylStructure"))
	  (item.set_feat seg "us_diphone_right" (format nil "_%s" name)))
      (if (and (member_string name '(w y l m n p t k))
	       (string-equal (item.feat seg "p.name") 's)
	       (item.relation.prev seg "SylStructure"))
	  (item.set_feat seg "us_diphone_right" (format nil "_%s" name)))
      (if (and (string-equal name 's)
	       (member_string (item.feat seg "n.name") '(w y l m n p t k))
	       (item.relation.next seg "SylStructure"))
	  (item.set_feat seg "us_diphone_left" (format nil "%s_" name)))
      (if (and (string-equal name 'hh)
	       (string-equal (item.feat seg "n.name") 'y))
	  (item.set_feat seg "us_diphone_left" (format nil "%s_" name)))
      (if (and (string-equal name 'y)
	       (string-equal (item.feat seg "p.name") 'hh))
	  (item.set_feat seg "us_diphone_right" (format nil "_%s" name)))
      (if (and (member_string name '(p t k b d g))
	       (member_string (item.feat seg "n.name") '(r w y l))
	       (item.relation.next seg "SylStructure"))
	  (item.set_feat seg "us_diphone_left" (format nil "%s_" name)))
      )
     ((string-equal "ah" (item.name seg))
      (item.set_feat seg "us_diphone" "aa"))

   )))

(define (akl_nz_cw_cg_voice_reset)
  "(akl_nz_cw_cg_voice_reset)
Reset global variables back to previous voice."
  (akl_nz_cw_cg::reset_phoneset)
  (akl_nz_cw_cg::reset_tokenizer)
  (akl_nz_cw_cg::reset_tagger)
  (akl_nz_cw_cg::reset_lexicon)
  (akl_nz_cw_cg::reset_phrasing)
  (akl_nz_cw_cg::reset_intonation)
  (akl_nz_cw_cg::reset_duration)
  (akl_nz_cw_cg::reset_f0model)
  (akl_nz_cw_cg::reset_other)
)

;;;  Full voice definition 
(define (voice_akl_nz_cw_cg_diphone)
"(voice_akl_nz_cw_cg_diphone)
Set speaker to cw2 in us from akl."
  ;; Select appropriate phone set
  (akl_nz_cw_cg::select_phoneset)

  ;; Select appropriate tokenization
  (akl_nz_cw_cg::select_tokenizer)

  ;; For part of speech tagging
  (akl_nz_cw_cg::select_tagger)

  (akl_nz_cw_cg::select_lexicon)

  (akl_nz_cw_cg::select_phrasing)

;  (akl_nz_cw_cg::select_intonation)

  (akl_nz_cw_cg::select_duration)

 (akl_nz_cw_cg::select_f0model)

  ;; Phrase prediction
;  (require 'phrase)
;  (Parameter.set 'Phrase_Method 'prob_models)
;  (set! phr_break_params english_phr_break_params)


;; Accent and tone prediction
  (require 'tobi)
  (set! int_tone_cart_tree f2b_int_tone_cart_tree)
  (set! int_accent_cart_tree f2b_int_accent_cart_tree)

  (set! postlex_vowel_reduce_cart_tree 
	postlex_vowel_reduce_cart_data)

;; F0 prediction - Taken from kal_diphone.scm - uses a statistically trained model f2b_f0
  (require 'f2bf0lr)
  (set! f0_lr_start f2b_f0_lr_start)
  (set! f0_lr_mid f2b_f0_lr_mid)
  (set! f0_lr_end f2b_f0_lr_end)
  (Parameter.set 'Int_Method Intonation_Tree)
  (set! int_lr_params
	'((target_f0_mean 225)  ;;changed by amelie ;; speaker's mean F0, CIW changed from 100
	(target_f0_std 40)     ;;changed by amelie ;; speaker's range, CIW changed from 14
	  (model_f0_mean 170) (model_f0_std 34)))
  (Parameter.set 'Int_Target_Method Int_Targets_LR)
  


  ;; Waveform synthesizer: UniSyn diphones
  (set! UniSyn_module_hooks (list akl_nz_cw_cg_diphone_fix))
  (set! us_abs_offset 0.0)
  (set! window_factor 1.0)
  (set! us_rel_offset 0.0)
  (set! us_gain 0.9)

  (Parameter.set 'Synth_Method 'UniSyn)
  (Parameter.set 'us_sigpr 'lpc)
  (us_db_select akl_nz_cw_cg_db_name)

  ;; This is where you can modify power (and sampling rate) if desired
  (set! after_synth_hooks nil)
;  (set! after_synth_hooks
;      (list
;        (lambda (utt)
;          (utt.wave.rescale utt 2.1))))

  ;; set callback to restore some original values changed by this voice
  (set! current_voice_reset akl_nz_cw_cg_voice_reset)

  (set! current-voice 'akl_nz_cw_cg_diphone)
)

(proclaim_voice
 'akl_nz_cw_cg_diphone
 '((language english)
   (gender female)
   (dialect newzealand)
   (description
    "cw's voice."
    )
   (builtwith festvox-1.3)))

(provide 'akl_nz_cw_cg_diphone)
