import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EspacioRequest } from '../../models/espacio.model';
import { EspacioService } from '../../services/espacio.service';
import { ToastService } from '../../../../shared/services/toast.service';

type ImportMode = 'plano' | 'csv';

@Component({
  selector: 'app-espacio-import',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="page">
      <div class="page-header">
        <div class="page-title">
          <h1>Importar espacios</h1>
          <p>Genera espacios desde un plano o carga una tabla CSV.</p>
        </div>
        <a routerLink="/espacios" class="btn btn-secondary">Volver a espacios</a>
      </div>

      <div class="card import-card">
        <div class="mode-switch" role="tablist" aria-label="Método de importación">
          <button type="button" class="btn" [class.btn-primary]="mode === 'plano'"
                  [class.btn-secondary]="mode !== 'plano'" (click)="setMode('plano')">
            <span class="material-symbols-outlined">map</span> Plano PDF o imagen
          </button>
          <button type="button" class="btn" [class.btn-primary]="mode === 'csv'"
                  [class.btn-secondary]="mode !== 'csv'" (click)="setMode('csv')">
            <span class="material-symbols-outlined">table_view</span> Tabla CSV
          </button>
        </div>

        <section *ngIf="mode === 'plano'" class="import-content">
          <h2>Generar desde filas y columnas</h2>
          <p class="help-text">Carga el plano como referencia, indica cuántas filas y plazas hay en cada fila y revisa los códigos antes de guardarlos.</p>
          <p class="help-text">El plano sirve como referencia durante esta importación; se guardan los espacios generados, no el archivo.</p>

          <label class="form-group">
            <span>Plano de parqueadero (PDF, PNG o JPG; máximo 10 MB)</span>
            <input type="file" accept=".pdf,.png,.jpg,.jpeg,application/pdf,image/png,image/jpeg"
                   class="form-control" (change)="onMapSelected($event)" data-testid="map-file-input" />
          </label>

          <div *ngIf="mapUrl" class="map-preview">
            <img *ngIf="!mapIsPdf" [src]="mapUrl" [alt]="'Vista previa del plano ' + mapFileName" />
            <a *ngIf="mapIsPdf" [href]="mapUrl" target="_blank" rel="noopener noreferrer">
              Abrir plano PDF: {{ mapFileName }}
            </a>
          </div>

          <div class="grid-fields">
            <label class="form-group">
              <span>Prefijo de espacios</span>
              <input class="form-control" [(ngModel)]="prefix" (ngModelChange)="updatePreview()"
                     maxlength="8" data-testid="prefix-input" />
            </label>
            <label class="form-group">
              <span>Filas</span>
              <input type="number" min="1" max="2000" class="form-control" [(ngModel)]="rows"
                     (ngModelChange)="updatePreview()" data-testid="rows-input" />
            </label>
            <label class="form-group">
              <span>Plazas por fila</span>
              <input type="number" min="1" max="2000" class="form-control" [(ngModel)]="columns"
                     (ngModelChange)="updatePreview()" data-testid="columns-input" />
            </label>
            <label class="form-group">
              <span>Piso</span>
              <input type="number" min="0" class="form-control" [(ngModel)]="floor"
                     (ngModelChange)="updatePreview()" data-testid="floor-input" />
            </label>
            <label class="form-group">
              <span>Tipo</span>
              <select class="form-control" [(ngModel)]="spaceType" (ngModelChange)="updatePreview()">
                <option value="CARRO">Carro</option>
                <option value="MOTO">Moto</option>
                <option value="CAMIONETA">Camioneta</option>
                <option value="CAMIÓN">Camión</option>
                <option value="DISCAPACITADO">Discapacitado</option>
                <option value="ELECTRICO">Eléctrico</option>
              </select>
            </label>
          </div>

          <div *ngIf="errorMessage" class="alert alert-danger" role="alert">{{ errorMessage }}</div>
          <ng-container *ngIf="spacesToImport.length">
            <p class="preview-count">Vista previa: <strong>{{ spacesToImport.length }}</strong> espacios disponibles para importar.</p>
            <div class="preview-list">
              <span *ngFor="let space of spacesToImport.slice(0, 40)" class="badge badge-neutral">{{ space.numero }}</span>
              <span *ngIf="spacesToImport.length > 40" class="help-text">y {{ spacesToImport.length - 40 }} más…</span>
            </div>
          </ng-container>
        </section>

        <section *ngIf="mode === 'csv'" class="import-content">
          <h2>Cargar tabla CSV</h2>
          <p class="help-text">Columnas requeridas: <code>numero</code>. Opcionales: <code>tipo</code>, <code>estado</code> y <code>piso</code>. Puedes guardar la hoja de Excel como CSV UTF-8 antes de cargarla.</p>
          <a class="template-link" download="plantilla-espacios.csv"
             href="data:text/csv;charset=utf-8,%EF%BB%BFnumero%3Btipo%3Bestado%3Bpiso%0AA-001%3BCARRO%3BLIBRE%3B1">
            Descargar plantilla CSV
          </a>
          <label class="form-group">
            <span>Archivo CSV (máximo 10 MB)</span>
            <input type="file" accept=".csv,text/csv" class="form-control"
                   (change)="onCsvSelected($event)" data-testid="csv-file-input" />
          </label>
          <div *ngIf="errorMessage" class="alert alert-danger" role="alert">{{ errorMessage }}</div>
          <p *ngIf="csvFileName && spacesToImport.length" class="preview-count">
            {{ csvFileName }}: <strong>{{ spacesToImport.length }}</strong> espacios listos para importar.
          </p>
          <div *ngIf="spacesToImport.length" class="preview-list">
            <span *ngFor="let space of spacesToImport.slice(0, 40)" class="badge badge-neutral">{{ space.numero }}</span>
            <span *ngIf="spacesToImport.length > 40" class="help-text">y {{ spacesToImport.length - 40 }} más…</span>
          </div>
        </section>

        <div class="form-actions">
          <button type="button" class="btn btn-primary" (click)="importSpaces()"
                  [disabled]="isSaving || !spacesToImport.length || (mode === 'plano' && !mapUrl)"
                  data-testid="btn-importar">
            <span class="material-symbols-outlined">cloud_upload</span>
            {{ isSaving ? 'Importando…' : 'Importar ' + spacesToImport.length + ' espacios' }}
          </button>
          <a routerLink="/espacios" class="btn btn-secondary">Cancelar</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .import-card { padding: 24px; }
    .mode-switch, .page-actions, .form-actions { display: flex; flex-wrap: wrap; gap: 10px; }
    .mode-switch { border-bottom: 1px solid var(--pf-border); padding-bottom: 18px; }
    .import-content { padding: 22px 0; }
    .import-content h2 { margin: 0 0 8px; color: var(--pf-ink); font-size: 19px; }
    .help-text { color: var(--pf-muted); font-size: 13px; margin: 0 0 16px; }
    .form-group { display: flex; flex-direction: column; gap: 7px; color: var(--pf-ink); font-size: 13px; }
    .grid-fields { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 16px; margin-top: 20px; }
    .map-preview { margin: 16px 0; max-height: 360px; overflow: auto; background: var(--pf-bg); padding: 10px; }
    .map-preview img { display: block; max-width: 100%; max-height: 340px; margin: auto; object-fit: contain; }
    .preview-count { color: var(--pf-ink); margin: 18px 0 8px; }
    .preview-list { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 16px; }
    .template-link { display: inline-block; margin: 0 0 16px; color: var(--pf-primary); font-weight: 600; }
    .form-actions { border-top: 1px solid var(--pf-border); padding-top: 18px; }
    @media (max-width: 600px) {
      .import-card { padding: 16px; }
      .mode-switch .btn { flex: 1; }
    }
  `]
})
export class EspacioImportComponent implements OnDestroy {
  mode: ImportMode = 'plano';
  mapFileName = '';
  mapUrl = '';
  mapIsPdf = false;
  csvFileName = '';
  prefix = 'P';
  rows = 1;
  columns = 1;
  floor = 1;
  spaceType = 'CARRO';
  private generatedSpaces: EspacioRequest[] = [];
  private csvSpaces: EspacioRequest[] = [];
  errorMessage = '';
  isSaving = false;

  get spacesToImport(): EspacioRequest[] {
    return this.mode === 'csv' ? this.csvSpaces : this.generatedSpaces;
  }

  constructor(
    private espacioService: EspacioService,
    private toast: ToastService,
    private router: Router
  ) {
    this.updatePreview();
  }

  ngOnDestroy(): void {
    this.releaseMapPreview();
  }

  setMode(mode: ImportMode): void {
    this.mode = mode;
    this.errorMessage = '';
    if (mode === 'plano') this.updatePreview();
  }

  onMapSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    const isPdf = file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf');
    const isImage = file.type.startsWith('image/') || /\.(png|jpe?g)$/i.test(file.name);
    if ((!isPdf && !isImage) || file.size > 10 * 1024 * 1024) {
      this.releaseMapPreview();
      this.mapFileName = '';
      this.mapIsPdf = false;
      this.errorMessage = 'Selecciona un plano PDF, PNG o JPG de máximo 10 MB.';
      input.value = '';
      return;
    }

    this.releaseMapPreview();
    this.mapFileName = file.name;
    this.mapIsPdf = isPdf;
    this.mapUrl = URL.createObjectURL(file);
    this.errorMessage = '';
    this.mode = 'plano';
  }

  async onCsvSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.csvSpaces = [];
    this.csvFileName = '';
    this.errorMessage = '';
    if (file.type !== 'text/csv' && !file.name.toLowerCase().endsWith('.csv')) {
      this.errorMessage = 'Guarda la hoja de Excel como archivo CSV UTF-8 antes de cargarla.';
      input.value = '';
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      this.errorMessage = 'El archivo CSV no puede superar 10 MB.';
      input.value = '';
      return;
    }

    try {
      this.csvSpaces = this.parseCsv(await file.text());
      this.csvFileName = file.name;
      this.mode = 'csv';
      input.value = '';
    } catch (error) {
      this.errorMessage = error instanceof Error ? error.message : 'No se pudo leer el archivo CSV.';
      input.value = '';
    }
  }

  updatePreview(): void {
    this.errorMessage = '';
    const total = Number(this.rows) * Number(this.columns);
    if (!Number.isInteger(this.rows) || !Number.isInteger(this.columns)
      || this.rows < 1 || this.columns < 1 || total > 2000) {
      this.generatedSpaces = [];
      this.errorMessage = 'Usa al menos una fila y una plaza por fila; el máximo por importación es 2000.';
      return;
    }
    if (!Number.isInteger(this.floor) || this.floor < 0) {
      this.generatedSpaces = [];
      this.errorMessage = 'El piso debe ser un número entero igual o mayor que cero.';
      return;
    }
    const normalizedPrefix = this.prefix.trim();
    if (!/^[A-Za-z0-9-]{1,8}$/.test(normalizedPrefix)) {
      this.generatedSpaces = [];
      this.errorMessage = 'El prefijo admite de 1 a 8 letras, números o guiones.';
      return;
    }

    this.generatedSpaces = Array.from({ length: total }, (_, index) => {
      const row = Math.floor(index / Number(this.columns));
      const column = index % Number(this.columns) + 1;
      const numero = `${normalizedPrefix}-${this.rowName(row)}-${String(column).padStart(2, '0')}`;
      return { numero, tipo: this.spaceType, estado: 'LIBRE', piso: Number(this.floor) };
    });
  }

  importSpaces(): void {
    if (!this.spacesToImport.length || this.isSaving) return;
    this.isSaving = true;
    this.espacioService.importar(this.spacesToImport).subscribe({
      next: (created) => {
        this.toast.success(`Se importaron ${created.length} espacios correctamente.`);
        this.router.navigate(['/espacios']);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'No se pudieron importar los espacios.';
        this.toast.error(this.errorMessage);
        this.isSaving = false;
      }
    });
  }

  private parseCsv(text: string): EspacioRequest[] {
    const lines = text.split(/\r?\n/, 1)[0];
    const delimiter = (lines.match(/;/g)?.length || 0) > (lines.match(/,/g)?.length || 0) ? ';' : ',';
    const records = this.readCsvRecords(text, delimiter).filter(record => record.some(value => value.trim()));
    if (records.length < 2) throw new Error('El CSV debe incluir encabezados y al menos un espacio.');

    const headers = records[0].map(value => this.normalizeHeader(value));
    const numberColumn = headers.findIndex(value => ['numero', 'codigo', 'number'].includes(value));
    if (numberColumn < 0) throw new Error('Agrega una columna "numero" o "codigo" al CSV.');
    const typeColumn = headers.indexOf('tipo');
    const stateColumn = headers.indexOf('estado');
    const floorColumn = headers.indexOf('piso');
    const spaces = records.slice(1).map((record, index) => {
      const numero = record[numberColumn]?.trim();
      if (!numero) throw new Error(`Falta el número del espacio en la fila ${index + 2}.`);
      const rawFloor = floorColumn < 0 ? '1' : record[floorColumn]?.trim();
      const piso = rawFloor ? Number(rawFloor) : 1;
      if (!Number.isInteger(piso) || piso < 0) {
        throw new Error(`El piso en la fila ${index + 2} debe ser un entero igual o mayor que cero.`);
      }
      return {
        numero,
        tipo: typeColumn < 0 ? 'CARRO' : record[typeColumn]?.trim() || 'CARRO',
        estado: stateColumn < 0 ? 'LIBRE' : record[stateColumn]?.trim() || 'LIBRE',
        piso
      };
    });
    if (spaces.length > 2000) throw new Error('El máximo por importación es de 2000 espacios.');
    return spaces;
  }

  private readCsvRecords(text: string, delimiter: string): string[][] {
    const records: string[][] = [];
    let record: string[] = [];
    let value = '';
    let quoted = false;
    for (let index = 0; index < text.length; index++) {
      const char = text[index];
      if (char === '"') {
        if (quoted && text[index + 1] === '"') {
          value += '"';
          index++;
        } else {
          quoted = !quoted;
        }
      } else if (char === delimiter && !quoted) {
        record.push(value);
        value = '';
      } else if ((char === '\n' || char === '\r') && !quoted) {
        if (char === '\r' && text[index + 1] === '\n') index++;
        record.push(value);
        records.push(record);
        record = [];
        value = '';
      } else {
        value += char;
      }
    }
    if (quoted) throw new Error('El CSV contiene comillas sin cerrar.');
    if (value || record.length) {
      record.push(value);
      records.push(record);
    }
    return records;
  }

  private normalizeHeader(value: string): string {
    return value.replace(/^\uFEFF/, '').trim().toLowerCase()
      .normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  }

  private rowName(index: number): string {
    let value = index + 1;
    let label = '';
    while (value > 0) {
      const remainder = (value - 1) % 26;
      label = String.fromCharCode(65 + remainder) + label;
      value = Math.floor((value - 1) / 26);
    }
    return label;
  }

  private releaseMapPreview(): void {
    if (this.mapUrl) URL.revokeObjectURL(this.mapUrl);
    this.mapUrl = '';
  }
}
