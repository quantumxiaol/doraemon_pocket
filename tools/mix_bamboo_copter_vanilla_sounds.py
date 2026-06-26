#!/usr/bin/env python3
"""Build a Bamboo Copter preview by mixing vanilla Minecraft sounds."""

from __future__ import annotations

import json
import math
import shutil
import struct
import subprocess
import tempfile
import wave
from pathlib import Path


SAMPLE_RATE = 44_100
CHANNELS = 1
ASSET_ROOT = Path("/Users/quantumxiaol/.gradle/caches/fabric-loom/assets")
ASSET_INDEX = ASSET_ROOT / "indexes/1.20.1-5.json"
OUTPUT_DIR = Path("sound_previews/bamboo_copter_vanilla_mix_v3")
PREVIEW_SECONDS = 7.0


def clamp(value: float, minimum: float, maximum: float) -> float:
	return max(minimum, min(maximum, value))


def asset_path(name: str) -> Path:
	data = json.loads(ASSET_INDEX.read_text())["objects"]
	asset_hash = data[name]["hash"]
	return ASSET_ROOT / "objects" / asset_hash[:2] / asset_hash


def run_sox(args: list[str]) -> None:
	sox = shutil.which("sox")
	if sox is None:
		raise RuntimeError("sox is required for this preview")
	subprocess.run([sox] + args, check=True)


def render_ogg(path: Path) -> None:
	run_sox([str(path), "-C", "4", str(path.with_suffix(".ogg"))])


def decode_to_wav(source: Path, target: Path, speed: float = 1.0) -> None:
	args = [str(source), "-r", str(SAMPLE_RATE), "-c", str(CHANNELS), "-b", "16", str(target)]
	if speed != 1.0:
		args.extend(["speed", str(speed)])
	run_sox(args)


def read_wav(path: Path) -> list[float]:
	with wave.open(str(path), "rb") as wav:
		frames = wav.readframes(wav.getnframes())
		values = struct.unpack("<" + "h" * (len(frames) // 2), frames)
	return [value / 32768.0 for value in values]


def write_wav(path: Path, samples: list[float]) -> None:
	path.parent.mkdir(parents=True, exist_ok=True)
	peak = max(max(abs(sample) for sample in samples), 0.001)
	scale = 0.74 / peak

	with wave.open(str(path), "wb") as wav:
		wav.setnchannels(CHANNELS)
		wav.setsampwidth(2)
		wav.setframerate(SAMPLE_RATE)
		for sample in samples:
			value = int(clamp(sample * scale, -1.0, 1.0) * 32767)
			wav.writeframesraw(struct.pack("<h", value))


def add_clip(target: list[float], clip: list[float], start: int, volume: float) -> None:
	for i, sample in enumerate(clip):
		index = start + i
		if 0 <= index < len(target):
			target[index] += sample * volume


def build_preview() -> None:
	OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
	total_samples = int(PREVIEW_SECONDS * SAMPLE_RATE)
	mix = [0.0] * total_samples

	with tempfile.TemporaryDirectory() as tmp_dir_name:
		tmp_dir = Path(tmp_dir_name)
		bee_wav = tmp_dir / "bee.wav"
		decode_to_wav(asset_path("minecraft/sounds/mob/bee/loop1.ogg"), bee_wav, speed=0.8)
		bee = read_wav(bee_wav)

		for start in range(0, total_samples, max(1, len(bee))):
			add_clip(mix, bee, start, 0.055)

		flap_names = [
			"minecraft/sounds/mob/phantom/flap1.ogg",
			"minecraft/sounds/mob/phantom/flap2.ogg",
			"minecraft/sounds/mob/phantom/flap3.ogg",
			"minecraft/sounds/mob/phantom/flap4.ogg",
			"minecraft/sounds/mob/phantom/flap5.ogg",
			"minecraft/sounds/mob/phantom/flap6.ogg",
		]
		flaps: list[list[float]] = []
		for index, name in enumerate(flap_names):
			flap_wav = tmp_dir / f"flap{index}.wav"
			decode_to_wav(asset_path(name), flap_wav, speed=1.85)
			flaps.append(read_wav(flap_wav))

		# 7 flaps per second: audible blade-like rhythm without becoming a buzz.
		interval = int(SAMPLE_RATE / 7.0)
		start_offset = int(0.30 * SAMPLE_RATE)
		n = 0
		for start in range(start_offset, total_samples, interval):
			flap = flaps[n % len(flaps)]
			phase = n * 0.73
			volume = 0.078 + 0.020 * (0.5 + 0.5 * math.sin(phase))
			add_clip(mix, flap, start, volume)
			n += 1

		start_wav = OUTPUT_DIR / "preview.wav"
		write_wav(start_wav, mix)
		render_ogg(start_wav)

		print(start_wav)
		print(start_wav.with_suffix(".ogg"))


if __name__ == "__main__":
	build_preview()
