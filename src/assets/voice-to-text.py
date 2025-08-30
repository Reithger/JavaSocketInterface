#!/usr/bin/env python3

# prerequisites: as described in https://alphacephei.com/vosk/install and also python module `sounddevice` (simply run command `pip install sounddevice`)
# Example usage using Dutch (nl) recognition model: `python test_microphone.py -m nl`
# For more help run: `python test_microphone.py -h`

# File was initially borrowed from https://github.com/alphacep/vosk-api/blob/master/python/example/test_microphone.py
# and further edited to my usecase by Ada

# pip3 install vosk


'''

As you speak, output the partial text to a txt file

Every .25 seconds, another program pulls from that txt file and
identifies any new text in the current partial compared to the
previous one.

Other program displays text in HTML format where newest addition to
partial is highlighted in some way to differentiate.

If partial text isn't too long, leave it up for ~10 seconds before
fading it away (use queue of text that has been added we can delete
parts of text from).

If a lot of text is happening, find occasional break-points to wipe
out old dialogue from and only show new dialogue so it won't be too
jarring of a visual change.

Java program that can hotswap formatting options (Runescape chat,
regular captions, other fancy stylings) which calls this program
into existence and establishes a mutually understood text file.
Similar to the Audio Reactive Talk Sprite.

'''

import queue
import sys
import sounddevice as sd
import socket

from vosk import Model, KaldiRecognizer

q = queue.Queue()
port = sys.argv[1]
sender = None

counter = 0

def connect():
    global sender
    sender = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sender.connect(("127.0.0.1", int(port)))
    sender.send("\nTAG LIST VTT\n".encode("utf-8"))
    sender.send("\nConnection Established\n".encode("utf-8"))

def callback(indata, frames, time, status):
    """This is called (from a separate thread) for each audio block."""
    if status:
        print(status, file=sys.stderr)
    q.put(bytes(indata))

def process(caption):
    try:
        sender.send(caption.encode("utf-8"))
    except:
        if(not sender is None):
            sender.close()
        connect()

try:
    device_info = sd.query_devices(None, "input")
    # soundfile expects an int, sounddevice provides a float:
    samplerate = int(device_info["default_samplerate"])
        
    model = Model(model_name="vosk-model-en-us-0.22-lgraph")
    #model = Model(model_name="vosk-model-en-us-0.22")
    #model = Model(lang="en-us")

    with sd.RawInputStream(samplerate=samplerate, blocksize = 8000, device=None,
            dtype="int16", channels=1, callback=callback):
        print("#" * 80)
        print("Press Ctrl+C to stop the recording")
        print("#" * 80)
        
        process("Voice Model Loaded and Ready")

        rec = KaldiRecognizer(model, samplerate)
        while True:
            data = q.get()
            if rec.AcceptWaveform(data):
                process(rec.Result())
            else:
                process(rec.PartialResult())

except KeyboardInterrupt:
    print("\nDone")
