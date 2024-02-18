
export class Utils {
  static getColor(index: number, max: number): string {
    const startColor = [135, 206, 235]; // RGB value for slightly darker pastel blue
    const endColor = [152, 251, 152];   // RGB value for slightly darker pastel green

    const r = Math.round(startColor[0] + (endColor[0] - startColor[0]) * index / (max - 1));
    const g = Math.round(startColor[1] + (endColor[1] - startColor[1]) * index / (max- 1));
    const b = Math.round(startColor[2] + (endColor[2] - startColor[2]) * index / (max - 1));

    return `rgb(${r}, ${g}, ${b})`;
  }
}
