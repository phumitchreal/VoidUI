param(
    [string]$SourceJar,
    [string]$RefMapEntry,
    [string]$OutPath
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$z = [System.IO.Compression.ZipFile]::OpenRead($SourceJar)
$e = $z.GetEntry($RefMapEntry)
$r = New-Object System.IO.StreamReader($e.Open())
$json = $r.ReadToEnd()
$r.Close()
$z.Dispose()

$o = $json | ConvertFrom-Json

function Parse-Value($v) {
    if ($v -match '^L([^;]+);([\.\w$<>]+)(\([^)]*\))?((?:\[L[^;]+;|\[[VZBCSIJFD]|[VZBCSIJFD])|:[LI\[][^;]*;?)?$') {
        return @{ Owner = $Matches[1]; Name = $Matches[2]; Desc = $Matches[3]; Tail = $Matches[4] }
    }
    return $null
}

function Build-FixedSection($section) {
    $out = @{}
    foreach ($cls in $section.psobject.Properties) {
        $map = @{}
        foreach ($k in $cls.Value.psobject.Properties) {
            $origKey = $k.Name
            $origVal = $k.Value
            $map[$origKey] = $origVal
            $p = Parse-Value $origVal
            if (-not $p) { continue }
            $fullVal = 'L{0};{1}{2}{3}' -f $p.Owner, $p.Name, $p.Desc, $p.Tail
            if ($origKey -match '^L([^;]+);(.+)$') {
                $keyOwner = $Matches[1]
                $keyRest = $Matches[2]
                $map["L$keyOwner;$keyRest"] = $fullVal
                if ($keyRest -match '^([\.\w$<>]+)(\([^)]*\))?([VZBCSIJFD]|\[)*$') {
                    $map[$Matches[1] + $Matches[2] + $Matches[3]] = $fullVal
                }
            } elseif ($origKey -match '^([\.\w$<>]+)(\([^)]*\))?$') {
                $map[$Matches[1] + $Matches[2]] = $fullVal
            }
            $map[$p.Name] = $fullVal
            $map["L$($p.Owner);$($p.Name)$($p.Desc)$($p.Tail)"] = $fullVal
        }
        $out[$cls.Name] = $map
    }
    return $out
}

$result = @{
    mappings = Build-FixedSection $o.mappings
    data = @{ searge = Build-FixedSection $o.data.searge }
}
$result | ConvertTo-Json -Depth 20 -Compress | Set-Content -Path $OutPath -Encoding UTF8
Write-Output "wrote $OutPath"