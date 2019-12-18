#!/bin/bash

echo Initializing the encryption engines
sleep 2
echo Vroom Vroom...
sleep 1
echo Ready for liftoff

if [ $# -lt 2 ]; then
  print_usage;
fi

function print_usage() {
  echo """Usage: encrypt [OPTION]... [ARGS]...
      -d                                set mode to decrypt
      -e                                set mode to encrypt
      -g                                generate keypair for encryption
      -f [path-to-file]                 set path for saving message to (default is current-dir/date_time) when encrypting
                                            when decrypting it saves the decrypted message to the file
      -h                                display this info and exit
  """
}