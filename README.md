![A Copper Golem on a stage playing music with the text "Golem DJ" in front of it](https://cdn.modrinth.com/data/cached_images/9ca2507a54e774db12663c552446dae8b4185a9a.webp)

<center>
  Is your world not quite musical enough? Let the golems play you your favourite songs.
</center>

<br>

## About

Golem DJ is a simple mod that allows Copper Golems to put music discs into jukeboxes.

### Normal Golems
By default, when a golem picks up a music disc from a chest, it will try to put the disc in the closest jukebox.
- If the jukebox already has a disc in it, the golem will wait at the jukebox until the disc is removed.
- If the golem can't find a jukebox, it will sort the disc into a chest.

### DJ Golems
If a Copper Golem has the word "DJ" in its name, it will _only_ pick-up music discs, ignoring all other items.
- It will try to put a disc into a jukebox, no matter the config settings.
- It will wait at a jukebox, no matter the config settings.
- It will not sort music discs, no matter the config settings.

<br>

## Compatibility

The mod is compatible with any mod that adds music discs which uses vanilla's ``JUKEBOX_PLAYABLE`` component.
> (This is the standard for music discs, so it should be compatible with most mods.)

#### Recommended mods to combine with Golem DJ:
- [Sculk Radio](https://modrinth.com/mod/sculk-radio) _by [Gjorgdy (me)](https://modrinth.com/user/Gjorgdy)_
- [AudioPlayer](https://modrinth.com/mod/audioplayer) _by [henkelmax](https://modrinth.com/user/henkelmax)_
- [Sound Physics Remastered](https://modrinth.com/mod/sound-physics-remastered) _by [henkelmax](https://modrinth.com/user/henkelmax)_

<br>

## Configuration

On its own, the mod will not create a config file.
To change settings, you can install [Fzzy Config](https://modrinth.com/mod/fzzy-config).

To load changes to the config file, you can use the vanilla ``/reload`` command.

```toml
# Whether all golems should try to put a disc into a jukebox.
useJukebox = true
# Whether all golems should wait at a jukebox if it can't put the disc in.
waitAtJukebox = true
# Whether a golem should sort a disc into a chest if it can't find a jukebox to put it in.
sortDiscIfNoJukebox = true
```